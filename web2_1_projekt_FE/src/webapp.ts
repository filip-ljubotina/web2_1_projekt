import express from 'express';
import path from 'path'
import { auth, requiresAuth } from 'express-openid-connect';
import dotenv from 'dotenv'
import { TicketData } from './types/types';
const axios = require('axios');
const bodyParser = require('body-parser');
dotenv.config()

const app = express();
app.set("views", path.join(__dirname, "views"));
app.set('view engine', 'pug');
app.use(bodyParser.urlencoded({ extended: true }));

const port = process.env.PORT || 3001;
const beUrl = process.env.BE_URL || 'http://localhost:8080';
app.use(express.static(path.join(__dirname, 'public')));

const config = {
  authRequired: false,
  secret: process.env.SECRET,
  baseURL: `https://web2-1-projekt-1.onrender.com`,
  clientID: process.env.OIDC_CLIENT_ID,
  issuerBaseURL: 'https://dev-pq6kcfoy6i780ddd.us.auth0.com',
  clientSecret: process.env.OIDC_CLIENT_SECRET,
  authorizationParams: {
    response_type: 'code',
  },
};

app.use(auth(config));

let lastGeneratedTicket: string | null = null;
let accessToken: string | null = null;

async function getAccessToken() {
  try {
    const response = await axios.post('https://dev-pq6kcfoy6i780ddd.us.auth0.com/oauth/token',
      {
        audience: 'FER-Web2 1_projekt-API',
        grant_type: 'client_credentials',
        client_id: process.env.MTOM_CLIENT_ID,
        client_secret: process.env.MTOM_CLIENT_SECRET
      },
      {
        headers: {
          'Content-Type': 'application/json'
        },
        responseType: 'json'
      }
    );

    return response.data.access_token;
  } catch (error) {
    console.error('Error fetching access token:', error);
    throw error;
  }
}

async function callTotalNumTickets() {
  if (!accessToken) {
    accessToken = await getAccessToken();
  }

  const response = await axios.get(`${beUrl}/api/totalTickets`, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'content-type': 'application/json',
    },
  });

  return response.data;
}

async function callGenerateTicket(ticketData: TicketData) {
  if (!accessToken) {
    accessToken = await getAccessToken();
  }
  const response = await axios.post(`${beUrl}/api/generateTicket`, ticketData, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'content-type': 'application/json',
    },
    responseType: 'arraybuffer',
  });

  return response.data;
}

async function callGetTicket(ticketUuid: string) {
  if (!accessToken) {
    accessToken = await getAccessToken();
  }

  const response = await axios.get(`${beUrl}/api/getTicketByUuid/${ticketUuid}`, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'content-type': 'application/json',
    }
  });

  return response.data;
}

app.get('/', function (req, res) {
  let numOfTickets;
  let username: string | undefined;

  callTotalNumTickets()
    .then((data) => {
      numOfTickets = data.toString();

      if (req.oidc.isAuthenticated()) {
        username = req.oidc.user?.name ?? req.oidc.user?.sub;
      }

      res.render('index', { numOfTickets, imgSrc: lastGeneratedTicket ?? null });
    })
    .catch((error) => {
      console.error('Error fetching tickets:', error);
      res.render('index', { username, tickets: 'Error fetching tickets', });
    });
});

app.get('/new-ticket', function (req, res) {
  res.render('new-ticket');
});

app.post('/new-ticket', function (req, res) {
  const vatin = req.body.vatin;
  const firstName = req.body.firstName;
  const lastName = req.body.lastName;

  const ticketData = {
    vatin: vatin,
    firstName: firstName,
    lastName: lastName
  };

  callGenerateTicket(ticketData)
    .then((data) => {
      const base64Image = Buffer.from(data, 'binary').toString('base64');
      const imgSrc = `data:image/png;base64,${base64Image}`;

      lastGeneratedTicket = imgSrc;

      res.redirect('/');
    })
    .catch((error) => {
      res.render('new-ticket', { error: error?.response?.data?.message || 'Error generating ticket' });
    });
});

app.get('/private', requiresAuth(), function (req, res) {
  var ticketUuid: string;
  if (typeof req.query.uuid === 'string') {
    ticketUuid = req.query.uuid;
  } else {
    res.status(400).send('Invalid or missing ticket UUID');
    return;
  }
  var userName = req.oidc.user?.name;

  callGetTicket(ticketUuid)
    .then((data) => {
      res.render('private', { userName, ticketData: data });
    })
    .catch((error) => {
      res.render('private', { userName, error: error?.response?.data?.message || 'Error fetching ticket' });
    });
});

app.listen(port, function () {
  console.log(`App listening on port ${port}!`);
});