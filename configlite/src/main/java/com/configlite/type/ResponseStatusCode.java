package com.configlite.type;

public interface ResponseStatusCode {

    int SUCCESS = 200;

    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int REQUEST_TIMEOUT = 408;
    int TOO_MANY_REQUESTS = 429;

    int INTERNAL_SERVER_ERROR = 500;
    int BAD_GATEWAY = 502;
    int SERVICE_UNAVAILABLE = 503;
    int GATEWAY_TIMEOUT = 504;

    int ERROR_BASE_URL = 601;
}

/*
Informational responses (100–199),
Successful responses (200–299),
Redirects (300–399),
Client errors (400–499),
and Server errors (500–599).
*/