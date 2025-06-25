# amcloud-gateway

## Description
amcloud-gateway is the API Gateway microservice for the amcloud-platform ecosystem.  
It centralizes and manages routing, authentication, and authorization for other microservices, enabling secure and efficient communication.

## Prerequisites

- Java JDK 17 or higher
- Maven 3.9+
- [Optional] Docker (for deployment)
- Git

## Repository Structure

```
amcloud-gateway/
├── spring-cloud-gateway/         # Main Spring Boot gateway source code
│   ├── src/
│   ├── pom.xml
│   └── ...
├── .github/                      # GitHub workflows, issue templates, etc.
├── docs/                         # Documentation and diagrams
└── README.md
```

## Environment Configuration

A `.env` file **must** be present at the root of `spring-cloud-gateway/` before running or testing the microservice.  
Example content:

```
# === Gateway Configuration ===
GATEWAY_SERVER_PORT=8080
GATEWAY_APPLICATION_NAME=gateway

# === IAM Configuration ===
IAM_ROUTE_URI=http://localhost:8081
IAM_ROUTE_PATH=/auth/**

# === Other Services Configuration ===
BILLING_ROUTE_URI=http://localhost:8082
BILLING_ROUTE_PATH=/billing/**

RESERVATION_ROUTE_URI=http://localhost:8083
RESERVATION_ROUTE_PATH=/reservations/**

INVITATION_ROUTE_URI=http://localhost:8084
INVITATION_ROUTE_PATH=/invitations/**

NOTIFICATION_ROUTE_URI=http://localhost:8085
NOTIFICATION_ROUTE_PATH=/notifications/**
```

> ⚠️ **Never commit sensitive values (tokens, passwords) into the repository.**  
> Use placeholders and configure secrets securely in your deployment environments.

## Installation & Build

```bash
git clone https://github.com/Project-In3-Uds/amcloud-gateway.git
cd amcloud-gateway/spring-cloud-gateway
mvn clean install
```

## Running the Microservice

You can start the gateway with:

```bash
mvn spring-boot:run
```

Or build the JAR and run:

```bash
mvn package
java -jar target/gateway-0.0.1-SNAPSHOT.jar
```

## Basic API Usage

By default, the gateway runs on `http://localhost:8080/`.

To route requests to a service:
```bash
curl http://localhost:8080/{service}/{endpoint}
```
Example:
```bash
curl http://localhost:8080/billing/list
```

## Technologies Used

- Java 17
- Spring Boot 3.x (API Gateway)
- Spring Cloud Gateway
- Maven
- Docker (optional)
- GitHub Actions (CI/CD)

## Architecture Overview

```mermaid
sequenceDiagram
    actor Utilisateur
    participant IAM as "IAM (Keycloak/Okta)"
    participant Gateway as "API Gateway"
    participant Microservice as "Microservice (Ex: Billing)"
    participant Invitation as "Invitation Service"
    participant Reservation as "Reservation Service"
    participant Notification as "Notification Service"
    participant Billing as "Billing Service"

    %% Initialisation du Gateway
    rect rgb(230, 255, 230)
        Note over Gateway, IAM: Initialisation du Gateway
        Gateway->>IAM: Demande de configuration JWKS (/realms/myrealm/protocol/openid-connect/certs)
        IAM-->>Gateway: Retourne le JWKS (clé publique pour validation JWT)
        Note right of Gateway: Le Gateway est maintenant prêt à valider les JWT.
    end

    %% Étape 1 - Authentification
    rect rgb(230, 255, 230)
        Note over Utilisateur, IAM: Étape 1 - Authentification
        Utilisateur->>IAM: POST /login (email/mot de passe)
        IAM-->>Utilisateur: Retourne un JWT (accès) + Refresh Token
        Note right of IAM: JWT contient:<br>sub, roles, scopes, exp, iss
    end

    %% Étape 2 - Requête vers un microservice (avec JWT)
    rect rgb(255, 255, 230)
        Note over Utilisateur, Microservice: Étape 2 - Requête vers un microservice (avec JWT)

        alt Token Valide et Autorisé
            Utilisateur->>Gateway: GET /activities (Header: Authorization: Bearer <JWT>)
            Gateway->>Gateway: 1. Valide la signature du JWT (via JWKS)
            Gateway->>Gateway: 2. Vérifie les scopes/rôles (ex: "activity:read")
            Gateway->>Microservice: Forward la requête (ajoute X-User-ID, X-Roles)
            Note right of Microservice: Utilise X-User-ID pour la logique métier
            Microservice-->>Gateway: Réponse (données ou erreur)
            Gateway-->>Utilisateur: Retourne le résultat
        else Token Invalide/expiré ou Accès refusé
            Utilisateur->>Gateway: GET /activities (Header: Authorization: Bearer <JWT Invalide>)
            Gateway->>Gateway: 1. Valide la signature du JWT (échec)
            Note left of Gateway: [Token Invalide/expiré]
            Gateway--xUtilisateur: 401 Unauthorized

            Utilisateur->>Gateway: GET /admin/dashboard (Header: Authorization: Bearer <JWT>)
            Gateway->>Gateway: 2. Vérifie les scopes/rôles (accès refusé)
            Note left of Gateway: [Accès refusé]
            Gateway--xUtilisateur: 403 Forbidden
        end
    end

    %% Étape 3 - Requêtes Spécifiques aux Microservices
    rect rgb(230, 255, 230)
        Note over Utilisateur, Billing: Étape 3 - Requêtes Spécifiques aux Microservices
        Utilisateur->>Gateway: Request to /billing/list
        Gateway->>Billing: Forward to Billing service
        Billing-->>Gateway: Response
        Gateway-->>Utilisateur: Response

        Utilisateur->>Gateway: Request to /reservations/create
        Gateway->>Reservation: Forward to Reservation service
        Reservation-->>Gateway: Response
        Gateway-->>Utilisateur: Response

        Utilisateur->>Gateway: Request to /notifications/send
        Gateway->>Notification: Forward to Notification service
        Notification-->>Gateway: Response
        Gateway-->>Utilisateur: Response

        Utilisateur->>Gateway: Request to /invitations/create
        Gateway->>Invitation: Forward to Invitation service
        Invitation-->>Gateway: Response
        Gateway-->>Utilisateur: Response
    end

    %% Cas Spécifique : Requêtes Directes vers IAM
    rect rgb(255, 230, 230)
        Note over Utilisateur, IAM: Cas Spécifique : Requêtes Directes vers IAM
        Utilisateur->>IAM: (Optionnel) POST /oauth/token (Refresh Token pour un nouveau JWT)
        IAM-->>Utilisateur: Retourne un nouveau JWT

        Microservice->>IAM: (Optionnel) Introspection de JWT
        IAM-->>Microservice: Résultat d'introspection
    end
```

## Known Issues / Limitations

- Requires IAM service for authentication and JWT validation.
- Hot-reloading of routes is dependent on client microservice capabilities.
- No rate-limiting or circuit breaker by default (consider adding these features in production).

## Support / Contact

- For questions or support, [open an issue](https://github.com/Project-In3-Uds/amcloud-gateway/issues).
- For real-time discussion, contact us at project.in3.uds@outlook.com.

## Contribution

We welcome contributions! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) before submitting a pull request.

## License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

## Credits

Developed by Project-In3-Uds contributors.  
Special thanks to all open-source libraries and the community!