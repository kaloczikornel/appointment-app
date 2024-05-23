Register a new client user
```json
{
  "name": "user",
  "password": "password",
  "email": "test@alma.hu",
  "role": "CLIENT"
}
```
Register a new provider user
```json
{
  "name": "provider",
  "password": "password",
  "email": "alma@korte.hu",
  "role": "PROVIDER"
}
```
Login
```json
{
  "email": "test@alma.hu",
  "password": "password"
}
```
Edit user
```json
{
  "name": "user",
  "password": "password",
  "email": "alma@korte.hu", 
  "role": "CLIENT"
}
```
Create an availability
```json
{
  "day": "MONDAY",
  "startTime": "2021-05-01T10:00:00",
  "endTime": "2021-05-01T12:00:00"
}
```
Create an appointment
```json
{
  "userId": 1,
  "providerId": 1,
  "startTime": "2021-05-01T10:00:00",
  "endTime": "2021-05-01T11:00:00",
  "day": "MONDAY"
}
```
