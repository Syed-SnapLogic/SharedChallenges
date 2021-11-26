# Java based Workday Credentialing Reference Application

## Sample Issuer: issuer

`/issuer` serves a post office page that will issue an address credential.

## Sample Verifier: verifier

`/verifier` will present a free ice cream coupon if a user can verify they live in Boulder.

# Running

Run `./gradlew run` to start the server pointing at `localhost:8000`.

Navigate to `http://localhost:8000/issuer` to issue a new address credential.

Navigate to `http://localhost:8000/verifier` to verify credentials.
