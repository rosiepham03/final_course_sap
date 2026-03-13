# Frontend Setup and Configuration

## Quick Start

### Step 1: Start Backend (if not already running)

```bash
cd srv
mvn spring-boot:run
```

Expected output:
```
Started Application in X seconds
```

### Step 2: Start Frontend

**Option A: Using Python (if Python 3 is installed)**
```bash
cd app
python -m http.server 3000
```

**Option B: Using Node.js (if Node.js is installed)**
```bash
cd app
npm install http-server -g
http-server . -p 3000
```

**Option C: Using PowerShell (Windows)**
```powershell
cd app
# Simple HTTP server using PowerShell
powershell -Command "cd (Get-Location).Path; python -m http.server 3000"
```

### Step 3: Open in Browser

Navigate to: `http://localhost:3000`

## Feature Walkthrough

### 1. Dashboard
- Shows overall system statistics
- Displays recent inspections
- Shows system health status
- Updates in real-time

### 2. Inspections Tab
- View all inspections in tabular format
- Search with filters (equipment name, status)
- Click eye icon to view details
- Click trash icon to delete
- Supports 100+ records with pagination

### 3. Equipment Tab
- Browse all registered equipment
- Card layout with key information
- Shows status (Active, Inactive, etc.)
- Equipment details include: type, location, serial number

### 4. Inspectors Tab
- View inspector profiles
- Shows: name, ID, department, email, certifications
- Quick reference for planning inspections

### 5. New Inspection Tab
- Form to create new inspection
- Dropdown selection for equipment and inspector
- Date pickers for inspection timeline
- Text areas for findings and safety issues
- Submit to create inspection record

## API Configuration

### Changing Backend URL

If your backend runs on a different port or server:

1. Open `app/app.js`
2. Find line: `apiUrl: 'http://localhost:8080/api'`
3. Change to your backend URL
4. Save and reload page

Example for remote server:
```javascript
apiUrl: 'http://your-server.com:8080/api'
```

## Data Loading

### Sample Data

Sample data is automatically loaded from CSV files:
- `db/data/equipment.csv` - 10 equipment records
- `db/data/inspector.csv` - 5 inspector records
- `db/data/inspection.csv` - 15 inspection records

If data doesn't load:
1. Check backend logs for errors
2. Verify CSV files exist in `db/data/`
3. Check network tab in browser DevTools (F12)

### Creating Sample Data

To create test data manually:

1. Go to "New Inspection" tab
2. Fill out the form with sample information
3. Click "Create Inspection"
4. Data is saved to H2 database

## Customization

### Changing Colors

Edit `app/style.css` and modify `:root` variables:

```css
:root {
    --primary-color: #0d6efd;      /* Main blue */
    --success-color: #198754;       /* Green */
    --danger-color: #dc3545;        /* Red */
    --warning-color: #ffc107;       /* Yellow */
    --info-color: #0dcaf0;          /* Cyan */
}
```

### Adding New Pages

1. Add new case in HTML: `<div v-if="currentPage === 'newPage'">`
2. Add to navigation: `@click.prevent="currentPage = 'newPage'"`
3. Add data and methods in `app.js`
4. Create corresponding API call

### Modifying Forms

Edit the form in the "New Inspection" section of `index.html`:
- Add/remove form fields
- Update `newInspection` object in `app.js`
- Add API integration in `submitInspection()` method

## Troubleshooting

### "Cannot GET /"

**Cause:** Server not running in app directory

**Fix:**
```bash
cd app
python -m http.server 3000
```

### API Calls Failing

**Check:**
1. Backend is running on port 8080
2. No CORS errors in console
3. Backend URL is correct in `app.js`
4. Network tab shows 200 status codes

**Solution:**
```javascript
// Add to browser console to test API
fetch('http://localhost:8080/api/inspections')
  .then(r => r.json())
  .then(d => console.log(d))
```

### Page Blank/Not Loading

**Try:**
1. Hard refresh: `Ctrl+Shift+R` (Windows) or `Cmd+Shift+R` (Mac)
2. Clear browser cache
3. Check console for JavaScript errors (F12)
4. Test in another browser

### Data Not Showing

**Check:**
1. Backend API responses in Network tab (F12)
2. Browser console for error messages
3. Backend logs for exceptions
4. H2 database exists: `db/data/db.mv.db`

## Performance Tips

### For Better Performance:

1. **Increase resources:**
   - Close unused browser tabs
   - Allocate more RAM to backend
   - Use SSD for database

2. **Optimize:**
   - Limit inspections per page
   - Cache API responses
   - Minify CSS/JS

3. **Monitor:**
   - Check Network tab (F12)
   - Monitor Response times
   - Check file sizes

## Security Notes

### Development Environment:

- ⚠️ No authentication required
- ⚠️ CORS allows all origins
- ⚠️ Data stored in H2 in-memory database
- ⚠️ Suitable for development only

### For Production:

- ✅ Add OAuth2/JWT authentication
- ✅ Use HTTPS only
- ✅ Restrict CORS to specific domains
- ✅ Use PostgreSQL/MySQL instead of H2
- ✅ Add input validation
- ✅ Implement role-based access control
- ✅ Add audit logging
- ✅ Regular security updates

## Advanced Configuration

### Connecting to Different Backend

Create `.env` file in app directory:

```
VUE_APP_API_URL=http://production-server.com:8080/api
```

Then update `app.js`:
```javascript
apiUrl: process.env.VUE_APP_API_URL || 'http://localhost:8080/api'
```

### Proxy Configuration

For development, you can use a proxy to avoid CORS issues:

Using http-server with proxy:
```bash
http-server . -p 3000 --proxy http://localhost:8080
```

### Build for Production

To optimize for deployment:

```bash
# Minify CSS/JS
npm install terser cleancss-cli -g
minify app.js -o app.min.js
cleancss app.css -o app.min.css

# Update HTML to reference minified files
```

## Monitoring & Logs

### Browser Console Logs

Check for debug information:
```javascript
// In browser console
axios.defaults.interceptors.request.use(config => {
  console.log('Request:', config);
  return config;
});
```

### Backend Logs

View backend application logs:
```bash
cd srv
tail -f target/logs/application.log
```

## Testing

### Manual Test Sequence

1. **Dashboard loads** → Check all stats display
2. **Inspections page** → Verify table populates
3. **Equipment page** → Check cards display
4. **Inspectors page** → Verify inspector list
5. **Create Inspection** → Test form submission
6. **Search** → Verify filtering works
7. **Mobile view** → Test responsive design

## Version Information

- Frontend: `1.0.0`
- Vue.js: `3.3.4`
- Bootstrap: `5.3.0`
- Requires: Node.js 14+, Python 3.6+, or modern browser

## Next Steps

1. Deploy frontend to web server
2. Configure backend for production
3. Add authentication system
4. Set up database persistence
5. Implement audit logging
6. Add email notifications
7. Create admin dashboard
8. Set up CI/CD pipeline
