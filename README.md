![DIF Logo](https://raw.githubusercontent.com/decentralized-identity/universal-registrar/master/docs/logo-dif.png)

# Universal Registrar Driver: v1

This is a [Universal Registrar](https://github.com/decentralized-identity/universal-registrar/) driver for **did:v1** identifiers.

## Specifications

* [Decentralized Identifiers](https://w3c.github.io/did-core/)
* [DID Method Specification](https://w3c-ccg.github.io/did-method-v1/)

## Build and Run (Docker)

```
docker build -f ./docker/Dockerfile . -t universalregistrar/driver-did-v1
docker run -p 9080:9080 universalregistrar/driver-did-v1
```

## Driver Environment Variables

The driver recognizes the following environment variables:

* `(none)`

## Driver Input Options

```
{
    "hostname": "",
    "ledger": "test",
    "keyType": "ed25519"
}
```

* `hostname`: Hostname of a ledger node.
* `ledger`: Name of the ledger. Options: `live`, `test`, `dev`.
* `keyType`: Type of keys to generate and use. Options: `ed25519`, `rsa`.

## Driver Output Metadata

```
{
    "didDocumentLocation": "/root/.dids/veres-test/registered/did%3Av1%3Atest%3Anym%3Az6MkjFYx9ss7FrEVmkJLmUgebJWAQTgHQeT2bPUZKqHin4sc.json"
}
```

* `didDocumentLocation`: The local file system location of the DID document.
