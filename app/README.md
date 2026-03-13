# Equipment Inspection Report System - Frontend

A modern, responsive web application for managing equipment inspection reports. Built with Vue.js 3, Bootstrap 5, and Axios.

## Features

- 📊 **Dashboard** - Real-time statistics and system status
- 📋 **Inspection Management** - View, search, and create inspections
- 🔧 **Equipment Registry** - Browse and manage equipment
- 👥 **Inspector Management** - View inspector details and certifications
- 📝 **Advanced Search** - Filter inspections by equipment, status, and date range
- 📄 **Report Generation** - Generate PDF inspection reports (via API)
- 📱 **Responsive Design** - Works seamlessly on desktop, tablet, and mobile
- ⚡ **Real-time Updates** - Dynamic data loading and instant feedback

## Technology Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Vue.js | 3.3.4 | Progressive UI framework |
| Bootstrap | 5.3.0 | CSS framework and components |
| Axios | 1.6.0 | HTTP client for API calls |
| Bootstrap Icons | 1.11.0 | Icon library |

## Getting Started

### Prerequisites

- Modern web browser (Chrome, Firefox, Safari, Edge)
- Backend API running on `http://localhost:8080`

### Installation

1. **Navigate to app directory:**
   ```bash
   cd app
   ```

2. **Install dependencies (optional):**
   ```bash
   npm install
   ```

3. **Start a local development server:**

   **Using Python:**
   ```bash
   python -m http.server 3000
   ```

   **Using Node.js (with http-server):**
   ```bash
   npm install -g http-server
   http-server . -p 3000
   ```

4. **Open in browser:**
   ```
   http://localhost:3000
   ```

## Project Structure

```
app/
├── index.html              # Main HTML template
├── app.js                  # Vue.js application logic
├── style.css              # Custom CSS styles
├── package.json           # Project metadata
└── README.md              # This file
```

## API Integration

The frontend communicates with the backend API at `http://localhost:8080/api`. 

### Available Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/inspections` | GET | Fetch all inspections |
| `/api/inspections/{id}` | GET | Get inspection details |
| `/api/equipment` | GET | Fetch all equipment |
| `/api/inspectors` | GET | Fetch all inspectors |
| `/api/search` | POST | Advanced search with filters |
| `/api/reports/generate` | POST | Generate inspection report |
| `/api/reports/export/{id}` | GET | Export report as PDF |

## Features Overview

### Dashboard
- View key statistics: total inspections, equipment, inspectors, pending reports
- System health status
- Recent inspection activity

### Inspections
- View all inspections in a data table
- Search and filter by equipment name and status
- View detailed inspection information
- Delete inspections (with confirmation)

### Equipment
- Browse all registered equipment
- View equipment details: type, location, serial number, status
- Card-based layout for easy scanning

### Inspectors
- View all inspectors
- Display inspector information: employee ID, department, email, certifications
- Quick reference for assigned inspections

### New Inspection
- Create complete inspection records
- Select equipment and inspector from dropdowns
- Set inspection dates and completion status
- Document findings and safety issues
- Add additional notes and comments

## Configuration

### Backend URL

To change the backend API URL, edit `app.js`:

```javascript
apiUrl: 'http://localhost:8080/api'  // Change this URL if needed
```

### Port Configuration

- **Frontend**: Change port in `npm run dev` command (default: 3000)
- **Backend**: Configure in `srv/src/main/resources/application.yaml`

## Browser Support

| Browser | Support |
|---------|---------|
| Chrome | ✅ Latest 2 versions |
| Firefox | ✅ Latest 2 versions |
| Safari | ✅ Latest 2 versions |
| Edge | ✅ Latest 2 versions |
| IE | ❌ Not supported |

## Responsive Breakpoints

The application is optimized for:
- 📱 Mobile: 320px - 480px
- 📱 Tablet: 481px - 768px
- 💻 Desktop: 769px - 1024px
- 🖥️ Large Desktop: 1025px+

## Troubleshooting

### Backend Connection Issues

**Problem:** "Error loading data. Make sure the backend is running."

**Solution:**
1. Ensure backend is running: `cd srv && mvn spring-boot:run`
2. Check backend URL in console (should be running on port 8080)
3. Verify CORS is enabled in backend
4. Check browser console for detailed error messages

### No Data Showing

**Problem:** Dashboard loads but shows no inspections, equipment, or inspectors

**Solution:**
1. Check if sample data CSV files exist in `db/data/`
2. Verify database initialization completed successfully
3. Check backend logs for data loading errors
4. Use `/api/inspections` endpoint directly in browser to test

### UI Not Rendering

**Problem:** Page appears blank or styles not loading

**Solution:**
1. Check browser console for JavaScript errors
2. Verify all CDN resources are loading (F12 → Network tab)
3. Clear browser cache and reload
4. Try in a different browser
5. Check that Vue.js and Bootstrap loaded correctly

## Development Tips

### Adding New Features

1. **Add data property** in the `data()` function
2. **Create method** in the `methods` object
3. **Add UI element** in the HTML template
4. **Bind with v-** directives (v-if, v-for, v-model, etc.)
5. **Test with backend API**

### Debugging

1. Open browser DevTools: `F12`
2. Check Console tab for errors
3. Inspect Network tab to see API calls
4. Check Vue DevTools extension for state debugging

### Performance

- To improve loading:
  - Paginate large data sets
  - Implement lazy loading
  - Cache API responses
  - Minimize bundle size

## Deployment

### Production Build

The frontend is a static SPA - no build step required. Copy the `app` folder to any web server:

```bash
# Using Apache
cp -r app/* /var/www/html/

# Using Nginx
cp -r app/* /usr/share/nginx/html/

# Using cloud storage (AWS S3, Azure Blob, etc.)
aws s3 sync app/ s3://my-bucket/
```

### Docker Deployment

```dockerfile
FROM nginx:alpine
COPY app /usr/share/nginx/html
EXPOSE 80
```

Build and run:
```bash
docker build -t inspection-frontend .
docker run -p 80:80 inspection-frontend
```

## Security Considerations

1. **API Authentication** - Currently no auth; add OAuth2/JWT in production
2. **HTTPS** - Use HTTPS in production environments
3. **CORS** - Backend must have CORS properly configured
4. **Input Validation** - Sanitize user inputs before sending to API
5. **Data Protection** - Implement role-based access control (RBAC)

## Performance Optimization

Current optimizations:
- Vue 3 for faster rendering
- Bootstrap CDN for reduced bundle size
- Minimal custom CSS
- Efficient API calls with Axios

Recommended next steps:
- Implement pagination for large datasets
- Add response caching
- Lazy load images and components
- Minify CSS/JS for production

## Testing

### Manual Testing Checklist

- [ ] All pages load without errors
- [ ] Data displays correctly on dashboard
- [ ] Can create new inspection
- [ ] Search filters work properly
- [ ] Responsive design on mobile
- [ ] API errors handled gracefully
- [ ] CSV data loads on startup

## Contributing

When contributing to the frontend:

1. Follow Vue.js style guide
2. Use Bootstrap utility classes
3. Test on multiple browsers
4. Update documentation
5. Keep components reusable

## License

MIT License - See LICENSE file for details

## Support

For issues or questions:
1. Check the [Backend API Documentation](../API_DOCUMENTATION.md)
2. Review server logs: `srv/target/logs/`
3. Check browser console for errors
4. Verify backend is running and accessible

## Changelog

### Version 1.0.0 (Initial Release)
- Dashboard with statistics
- Inspection management
- Equipment registry
- Inspector directory
- Advanced search
- Report generation interface
- Responsive design
- Vue 3 + Bootstrap 5 stack
