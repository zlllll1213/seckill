import http from 'k6/http'
import { check, group, sleep } from 'k6'
import { Rate, Trend } from 'k6/metrics'

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:8080'
const USERNAME = __ENV.USERNAME || 'admin'
const PASSWORD = __ENV.PASSWORD || 'admin123'
const ACTIVITY_ID = __ENV.ACTIVITY_ID || '1'
const INCLUDE_SECKILL = (__ENV.INCLUDE_SECKILL || 'false').toLowerCase() === 'true'
const THINK_TIME = Number(__ENV.THINK_TIME || '1')
const SMOKE = (__ENV.SMOKE || 'false').toLowerCase() === 'true'
const LOGIN_EACH_ITERATION = (__ENV.LOGIN_EACH_ITERATION || 'false').toLowerCase() === 'true'

const businessFailures = new Rate('business_failures')
const loginDuration = new Trend('login_duration')

export const options = SMOKE ? {
  scenarios: {
    smoke: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: '30s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1500'],
    business_failures: ['rate<0.01'],
  },
} : {
  scenarios: {
    mixed_read_flow: {
      executor: 'ramping-vus',
      stages: [
        { duration: '20s', target: 10 },
        { duration: '40s', target: 30 },
        { duration: '20s', target: 0 },
      ],
      gracefulRampDown: '10s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800', 'p(99)<1500'],
    login_duration: ['p(95)<900'],
    business_failures: ['rate<0.05'],
  },
}

const jsonHeaders = {
  'Content-Type': 'application/json',
}

let loggedIn = false
let authCookie = ''

function parseJson(res) {
  try {
    return res.json()
  } catch {
    return null
  }
}

function okResult(res) {
  const body = parseJson(res)
  return res.status === 200 && body && body.code === 200
}

function recordBusinessResult(res, expected = true) {
  businessFailures.add(expected ? !okResult(res) : false)
}

function extractAuthCookie(res) {
  const setCookie = String(res.headers['Set-Cookie'] || '')
  const match = setCookie.match(/jwt=[^;]+/)
  return match ? match[0] : ''
}

function authParams(name) {
  return {
    headers: authCookie ? { Cookie: authCookie } : {},
    tags: { name },
  }
}

function login() {
  const res = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ username: USERNAME, password: PASSWORD }),
    { headers: jsonHeaders, tags: { name: 'POST /api/auth/login' } },
  )
  loginDuration.add(res.timings.duration)
  check(res, {
    'login status is 200': r => r.status === 200,
    'login business code is 200': okResult,
    'login sets jwt cookie': r => String(r.headers['Set-Cookie'] || '').includes('jwt='),
  })
  recordBusinessResult(res)
  authCookie = extractAuthCookie(res)
  return res
}

function ensureLoggedIn() {
  if (LOGIN_EACH_ITERATION || !loggedIn) {
    const res = login()
    loggedIn = okResult(res)
    return res
  }
  return null
}

export default function () {
  group('auth flow', () => {
    ensureLoggedIn()
    const me = http.get(`${BASE_URL}/api/auth/me`, authParams('GET /api/auth/me'))
    check(me, {
      'me status is 200': r => r.status === 200,
      'me business code is 200': okResult,
    })
    recordBusinessResult(me)
  })

  group('catalog read flow', () => {
    const products = http.get(`${BASE_URL}/api/products?page=1&size=10`, {
      tags: { name: 'GET /api/products' },
    })
    check(products, {
      'products status is 200': r => r.status === 200,
      'products business code is 200': okResult,
    })
    recordBusinessResult(products)

    const activities = http.get(`${BASE_URL}/api/activities`, {
      tags: { name: 'GET /api/activities' },
    })
    check(activities, {
      'activities status is 200': r => r.status === 200,
      'activities business code is 200': okResult,
    })
    recordBusinessResult(activities)
  })

  group('member read flow', () => {
    const orders = http.get(`${BASE_URL}/api/orders`, authParams('GET /api/orders'))
    check(orders, {
      'orders status is 200': r => r.status === 200,
      'orders business code is 200': okResult,
    })
    recordBusinessResult(orders)
  })

  if (INCLUDE_SECKILL) {
    group('seckill write flow', () => {
      const seckill = http.post(`${BASE_URL}/api/seckill/${ACTIVITY_ID}`, null, {
        headers: authCookie ? { Cookie: authCookie } : {},
        tags: { name: 'POST /api/seckill/:id' },
      })
      check(seckill, {
        'seckill http status is 200': r => r.status === 200,
      })
      // The same user can only participate once, so business failures are expected under load.
      recordBusinessResult(seckill, false)

      const result = http.get(`${BASE_URL}/api/seckill/result/${ACTIVITY_ID}`, authParams('GET /api/seckill/result/:id'))
      check(result, {
        'seckill result http status is 200': r => r.status === 200,
      })
      recordBusinessResult(result, false)
    })
  }

  sleep(THINK_TIME)
}
