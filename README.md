# SponsorBox Backend

Backend API for **SponsorBox**, a platform connecting TikTok creators with partner companies for sponsored content deals.

Built with **Spring Boot 3.2**, **Spring Security + JWT**, and **MySQL**.

## Tech Stack

- **Java 17** / **Spring Boot 3.2.3**
- **Spring Data JPA** (Hibernate)
- **Spring Security** (stateless JWT authentication)
- **MySQL** (via XAMPP)
- **Lombok**
- **JJWT 0.11.5**
- **Maven**

## Architecture

```
com.sponsorbox
├── controllers/       # REST endpoints
├── models/            # JPA entities & enums
├── repositories/      # Spring Data repositories
├── services/          # Business logic
└── security/          # JWT & Spring Security config
```

## Data Model

```
User 1──1 CreatorProfile
User 1──1 PartnerCompany
CreatorProfile 1──* Deal
PartnerCompany 1──* Deal
Deal 1──* Message
CreatorProfile 1──* SuccessShowcase
SuccessShowcase 1──* Testimonial
```

### Roles

| Role | Description |
|------|-------------|
| `CREATOR` | TikTok content creator |
| `PARTNER` | Sponsor company |
| `ADMIN` | Platform administrator |

### Deal Lifecycle

```
PENDING → ESCROW_FUNDED → ACCEPTED → VIDEO_SUBMITTED → COMPLETED
```

Each transition is role-guarded: the partner funds escrow and completes, the creator accepts and submits the video.

## API Endpoints

### Authentication (`/api/auth`) — Public

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register a user (any role) |
| POST | `/api/auth/register-creator` | Register as creator (creates user + profile) |
| POST | `/api/auth/login` | Login, returns JWT |

### Creators (`/api/creators`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/profile` | CREATOR | Create creator profile |
| PUT | `/profile/{userId}` | CREATOR | Update profile |
| GET | `/verify-tiktok?handle=&followers=` | Public | Check TikTok eligibility |
| GET | `/eligible` | Public | List verified creators |
| GET | `/pending` | ADMIN | List pending creators |
| POST | `/{creatorId}/verify` | ADMIN | Verify a creator |
| POST | `/{creatorId}/reject` | ADMIN | Reject a creator |
| GET | `/{userId}` | Authenticated | Get creator profile |
| POST | `/{creatorId}/successes` | CREATOR | Add success showcase |
| GET | `/{creatorId}/successes` | Authenticated | List showcases |
| DELETE | `/successes/{successId}` | CREATOR | Delete showcase |
| POST | `/successes/{showcaseId}/testimonials` | PARTNER/ADMIN | Add testimonial |
| GET | `/successes/{showcaseId}/testimonials` | Authenticated | List testimonials |
| GET | `/{creatorId}/testimonials` | Authenticated | All testimonials for a creator |
| DELETE | `/testimonials/{testimonialId}` | CREATOR | Delete testimonial |

### Partners (`/api/partners`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/register` | PARTNER | Register a company |
| POST | `/{partnerId}/verify` | ADMIN | Verify a partner |
| GET | `/{userId}` | PARTNER/ADMIN | Get partner info |

### Deals (`/api/deals`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/create` | PARTNER | Create a deal |
| POST | `/{dealId}/fund-escrow` | PARTNER | Fund escrow |
| POST | `/{dealId}/accept` | CREATOR | Accept deal |
| POST | `/{dealId}/submit-video` | CREATOR | Submit TikTok video |
| POST | `/{dealId}/complete` | PARTNER | Complete deal |
| GET | `/{dealId}` | Participant/ADMIN | Get deal details |
| GET | `/creator/{creatorId}` | CREATOR/ADMIN | Deals by creator |
| GET | `/partner/{partnerId}` | PARTNER/ADMIN | Deals by partner |

### Messages (`/api/messages`)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/send` | CREATOR/PARTNER | Send a message in a deal |
| GET | `/deal/{dealId}` | Participant/ADMIN | Get deal messages |

## Getting Started

### Prerequisites

- **Java 17+**
- **Maven**
- **MySQL** (via XAMPP or standalone)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/odieng-oumy/SponsorBOX.git
   cd SponsorBOX
   ```

2. **Start MySQL** (XAMPP or standalone) on port `3306`

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   The database `sponsorbox_db` is created automatically.

4. **Default admin account** (created on first startup):
   ```
   Email:    admin@sponsorbox.com
   Password: admin123
   ```

The API runs on `http://localhost:8080`.

### CORS

Allowed origins: `http://localhost:3000`, `http://localhost:5173`

## Business Rules

- **Creator eligibility**: minimum 5,000 TikTok followers
- **Verification code**: each creator gets a unique `SB-XXXXXX` code for TikTok bio verification
- **Partner auto-rejection**: companies with generic email domains (gmail.com, yahoo.com, hotmail.com) are automatically rejected
- **Ownership enforcement**: users can only access their own data; admins can access everything
