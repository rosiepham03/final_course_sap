const { createApp } = Vue;

const app = createApp({
    data() {
        return {
            currentPage: 'dashboard',
            apiUrl: 'http://localhost:8080/api',
            
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
                const response = await axios.get(`${this.apiUrl}/equipment`);
                this.equipment = response.data || [];
                this.stats.totalEquipment = this.equipment.length;
            } catch (error) {
                console.error('Error loading equipment:', error);
            }
        },
        
        // Load inspectors
        async loadInspectors() {
            try {
                const response = await axios.get(`${this.apiUrl}/inspectors`);
                this.inspectors = response.data || [];
                this.stats.totalInspectors = this.inspectors.length;
            } catch (error) {
                console.error('Error loading inspectors:', error);
            }
        },
        
        // Load inspections
        async loadInspections() {
            try {
                const response = await axios.get(`${this.apiUrl}/inspections`);
                this.inspections = response.data || [];
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
        
        // Search inspections
        async searchInspections() {
            try {
                const params = {};
                if (this.filters.equipmentName) params.equipmentName = this.filters.equipmentName;
                if (this.filters.inspectorId) params.inspectorId = this.filters.inspectorId;
                if (this.filters.dateFrom) params.dateFrom = this.filters.dateFrom;
                if (this.filters.dateTo) params.dateTo = this.filters.dateTo;
                if (this.filters.status) params.status = this.filters.status;
                
                const response = await axios.get(`${this.apiUrl}/search`, { params });
                this.inspections = response.data || [];
                
                this.showAlert('Search completed', 'info');
            } catch (error) {
                console.error('Error searching inspections:', error);
                this.showAlert('Search failed', 'danger');
            }
        },
        
        // Submit new inspection
        async submitInspection() {
            try {
                const payload = {
                    inspectionId: 'INS-' + new Date().getTime(),
                    equipmentId: this.newInspection.equipmentId,
                    inspectorId: this.newInspection.inspectorId,
                    inspectionDate: this.newInspection.inspectionDate,
                    completionDate: this.newInspection.completionDate || null,
                    status: this.newInspection.status,
                    findings: this.newInspection.findings,
                    safetyIssues: this.newInspection.safetyIssues,
                    notes: this.newInspection.notes
                };
                
                const response = await axios.post(`${this.apiUrl}/reports/generate`, payload);
                
                this.showAlert('Inspection created successfully!', 'success');
                this.resetInspectionForm();
                this.currentPage = 'inspections';
                await this.loadInspections();
            } catch (error) {
                console.error('Error creating inspection:', error);
                this.showAlert('Failed to create inspection', 'danger');
            }
        },
        
        // View inspection details
        async viewInspectionDetails(inspectionId) {
            try {
                const response = await axios.get(`${this.apiUrl}/inspections/${inspectionId}`);
                console.log('Inspection details:', response.data);
                this.showAlert('Details loaded (check console)', 'info');
            } catch (error) {
                console.error('Error loading inspection details:', error);
                this.showAlert('Failed to load details', 'danger');
            }
        },
        
        // Delete inspection
        async deleteInspection(inspectionId) {
            if (confirm('Are you sure you want to delete this inspection?')) {
                try {
                    await axios.delete(`${this.apiUrl}/inspections/${inspectionId}`);
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
