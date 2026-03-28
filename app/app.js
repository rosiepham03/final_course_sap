const { createApp } = Vue;

const app = createApp({
    data() {
        return {
            currentPage: 'dashboard',
            apiUrl: 'http://localhost:8080/odata/v4/InspectionService',
            
            // Data collections
            inspections: [],
            equipment: [],
            inspectors: [],
            recentInspections: [],
            
            // Statistics
            stats: {
                totalInspections: 0,
                totalEquipment: 0,
                totalInspectors: 0,
                pendingReports: 0
            },
            
            // Filters
            filters: {
                equipmentName: '',
                inspectorId: '',
                dateFrom: '',
                dateTo: '',
                status: ''
            },
            
            // New Inspection Form
            newInspection: {
                equipmentId: '',
                inspectorId: '',
                inspectionDate: this.getCurrentDate(),
                completionDate: '',
                status: 'PENDING',
                findings: '',
                safetyIssues: '',
                notes: ''
            },
            
            // Alert
            alert: {
                show: false,
                type: 'info',
                message: ''
            }
        };
    },
    
    methods: {
        // Initialize data
        async initializeData() {
            try {
                await this.loadEquipment();
                await this.loadInspectors();
                await this.loadInspections();
                await this.loadStats();
            } catch (error) {
                console.error('Error initializing data:', error);
                this.showAlert('Error loading data. Make sure the backend is running.', 'danger');
            }
        },
        
        // Load equipment
        async loadEquipment() {
            try {
                const response = await axios.get(`${this.apiUrl}/Equipment`);
                // Handle OData response format
                const data = response.data.value || response.data;
                this.equipment = Array.isArray(data) ? data : [];
                this.stats.totalEquipment = this.equipment.length;
            } catch (error) {
                console.error('Error loading equipment:', error);
            }
        },
        
        // Load inspectors
        async loadInspectors() {
            try {
                const response = await axios.get(`${this.apiUrl}/Inspector`);
                // Handle OData response format
                const data = response.data.value || response.data;
                this.inspectors = Array.isArray(data) ? data : [];
                this.stats.totalInspectors = this.inspectors.length;
            } catch (error) {
                console.error('Error loading inspectors:', error);
            }
        },
        
        // Load inspections
        async loadInspections() {
            try {
                const response = await axios.get(`${this.apiUrl}/Inspection`);
                // Handle OData response format
                const data = response.data.value || response.data;
                let inspections = Array.isArray(data) ? data : [];
                // Filter out invalid UUIDs
                inspections = inspections.filter(i => this.isValidUUID(i.ID));
                this.inspections = inspections;
                this.recentInspections = [...this.inspections].sort((a, b) => {
                    return new Date(b.inspectionDate) - new Date(a.inspectionDate);
                });
                this.stats.totalInspections = this.inspections.length;
            } catch (error) {
                console.error('Error loading inspections:', error);
            }
        },
        
        // Load statistics
        async loadStats() {
            this.stats.totalInspections = this.inspections.length;
            this.stats.totalEquipment = this.equipment.length;
            this.stats.totalInspectors = this.inspectors.length;
            this.stats.pendingReports = this.inspections.filter(i => i.status !== 'COMPLETED').length;
        },
        
        // Validate UUID format (hex only: 0-9, a-f)
        isValidUUID(uuid) {
            const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
            return uuidRegex.test(uuid);
        },
        
        // Search inspections
        async searchInspections() {
            try {
                const search = this.filters.equipmentName || '';
                const status = this.filters.status || '';
                const equipmentId = '';
                const inspectorId = this.filters.inspectorId || '';
                const inspectionDateFrom = this.filters.dateFrom ? `'${this.filters.dateFrom}'` : 'null';
                const inspectionDateTo = this.filters.dateTo ? `'${this.filters.dateTo}'` : 'null';
                const equipmentName = this.filters.equipmentName || '';
                const limit = 100;
                const offset = 0;
                
                const url = `${this.apiUrl}/searchInspections(search='${encodeURIComponent(search)}',status='${encodeURIComponent(status)}',equipmentId='${encodeURIComponent(equipmentId)}',inspectorId='${encodeURIComponent(inspectorId)}',inspectionDateFrom=${inspectionDateFrom},inspectionDateTo=${inspectionDateTo},equipmentName='${encodeURIComponent(equipmentName)}',limit=${limit},offset=${offset})`;
                
                const response = await axios.get(url);
                const data = response.data.value || response.data;
                this.inspections = Array.isArray(data) ? data : [];
                this.showAlert('Search completed', 'info');
            } catch (error) {
                console.error('Error searching inspections:', error);
                this.showAlert('Search failed', 'danger');
            }
        },
        
        // Submit new inspection
        async submitInspection() {
            try {
                // Validate required fields
                if (!this.newInspection.equipmentId) {
                    this.showAlert('Please select equipment', 'warning');
                    return;
                }
                if (!this.newInspection.inspectorId) {
                    this.showAlert('Please select inspector', 'warning');
                    return;
                }

                // Create inspection record in database
                const inspectionData = {
                    ID: this.generateUUID(), // Generate a proper UUID
                    equipment_ID: this.newInspection.equipmentId,
                    inspector_ID: this.newInspection.inspectorId,
                    inspectionDate: this.newInspection.inspectionDate,
                    completionDate: this.newInspection.completionDate || this.newInspection.inspectionDate,
                    status: this.newInspection.status || 'Pending',
                    findings: this.newInspection.findings || '',
                    safetyIssues: this.newInspection.safetyIssues || '',
                    notes: this.newInspection.notes || ''
                };

                // POST to Inspection entity to create it
                const createResponse = await axios.post(`${this.apiUrl}/Inspection`, inspectionData);
                const createdInspectionId = createResponse.data.ID;

                this.showAlert('Inspection created successfully!', 'success');
                this.resetInspectionForm();
                this.currentPage = 'inspections';
                await this.loadInspections();
            } catch (error) {
                console.error('Error creating inspection:', error);
                this.showAlert('Failed to create inspection: ' + (error.response?.data?.message || error.message), 'danger');
            }
        },
        
        // View inspection details - FIXED UUID validation + OData key format
        async viewInspectionDetails(inspectionId) {
            try {
                if (!this.isValidUUID(inspectionId)) {
                    console.warn('Invalid UUID:', inspectionId);
                    this.showAlert('Invalid inspection ID format', 'danger');
                    return;
                }
                const response = await axios.get(`${this.apiUrl}/Inspection(ID=${inspectionId})`);  
                console.log('Inspection details:', response.data);
                this.showAlert('Details loaded (check console)', 'info');
            } catch (error) {
                console.error('Error loading inspection details:', error);
                this.showAlert('Failed to load details', 'danger');
            }
        },
        
        // Delete inspection - FIXED UUID validation + OData key format
        async deleteInspection(inspectionId) {
            if (!this.isValidUUID(inspectionId)) {
                console.warn('Invalid UUID:', inspectionId);
                this.showAlert('Invalid inspection ID format', 'danger');
                return;
            }
            if (confirm('Are you sure you want to delete this inspection?')) {
                try {
                    await axios.delete(`${this.apiUrl}/Inspection(ID=${inspectionId})`);  
                    this.showAlert('Inspection deleted', 'success');
                    await this.loadInspections();
                } catch (error) {
                    console.error('Error deleting inspection:', error);
                    this.showAlert('Failed to delete inspection', 'danger');
                }
            }
        },
        
        // Utility methods
        formatDate(dateString) {
            if (!dateString) return '-';
            const date = new Date(dateString);
            return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
        },
        
        getCurrentDate() {
            const today = new Date();
            return today.toISOString().split('T')[0];
        },
        
        showAlert(message, type = 'info') {
            this.alert.message = message;
            this.alert.type = type;
            this.alert.show = true;
            setTimeout(() => {
                this.alert.show = false;
            }, 5000);
        },
        
        resetInspectionForm() {
            this.newInspection = {
                equipmentId: '',
                inspectorId: '',
                inspectionDate: this.getCurrentDate(),
                completionDate: '',
                status: 'PENDING',
                findings: '',
                safetyIssues: '',
                notes: ''
            };
        },
        
        // Generate UUID
        generateUUID() {
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                const r = Math.random() * 16 | 0;
                const v = c === 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        },
        
        // Print search results - FIXED Base64 decoding + blob creation
        async printSearchResults() {
            try {
                if (this.inspections.length === 0) {
                    this.showAlert('No inspections to print', 'warning');
                    return;
                }
                
                const inspectionIds = this.inspections.map(i => i.ID);
                console.log('Printing ' + inspectionIds.length + ' inspections');
                
                const response = await axios.post(`${this.apiUrl}/printInspections`, 
                    { inspectionIds: inspectionIds }
                );
                
                // Handle Base64 response from OData
                let pdfBase64 = response.data;
                if (response.data.value) {
                    pdfBase64 = response.data.value;
                }
                
                // Decode Base64 to binary
                const binaryString = atob(pdfBase64);
                const bytes = new Uint8Array(binaryString.length);
                for (let i = 0; i < binaryString.length; i++) {
                    bytes[i] = binaryString.charCodeAt(i);
                }
                
                // Create blob and open
                const blob = new Blob([bytes], { type: 'application/pdf' });
                const url = URL.createObjectURL(blob);
                window.open(url);
                
                this.showAlert('PDF opened in new window', 'success');
            } catch (error) {
                console.error('Error printing inspections:', error);
                this.showAlert('Failed to print inspections', 'danger');
            }
        }
    },
    
    watch: {
        currentPage(newPage) {
            if (newPage === 'inspections') {
                this.loadInspections();
            } else if (newPage === 'equipment') {
                this.loadEquipment();
            } else if (newPage === 'inspectors') {
                this.loadInspectors();
            } else if (newPage === 'newInspection') {
                this.resetInspectionForm();
            }
        }
    },
    
    mounted() {
        this.initializeData();
    }
});

app.mount('#app');
