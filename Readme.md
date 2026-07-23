# DeskFlow

## Run

```bash
mvn spring-boot:run
```

## Web

1. Open `/` — login with employee name
2. Redirects to `/app.html?employee=Your%20Name`
3. My Bookings This Week — Mon–Sun for that employee
4. Cancel only works for your own bookings (API returns 403 otherwise)

Try: `Anna Kowalska`
