# Money Tracker MVP

Private, mobile-first personal money tracker based on the Figma design.

## Included
- Dashboard with monthly credit, spending, remaining balance and category breakdown
- Add Expense
- Add Credit
- Transaction history with search and filters
- Delete transaction
- Custom categories
- Settings
- Local-only persistence using browser localStorage
- Installable PWA shell + offline cache
- No login, backend, analytics, or cloud sync
- Notifications intentionally deferred

## Run locally
Requires Node.js.

```bash
npx serve .
```

Open the printed URL in a browser. For phone installation, use an HTTPS deployment and choose **Add to Home screen / Install app** in the browser.

## Data privacy
Transactions are stored in the browser's local storage on the device. Clearing browser/site data will remove them. There is no server-side copy.

## Note about reminders
The UI includes reminder settings, but actual scheduled notifications are intentionally not implemented yet.

## Notifications
The app now requests browser notification permission and includes a daily reminder setting plus a test notification. The reminder is scheduled while the PWA is active; fully reliable scheduled notifications after the browser/PWA has been completely terminated require native alarm support or a push/backend service.
