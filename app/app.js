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
            },

            // Print / view preview (modal + HTML, like demo.html)
            previewModalOpen: false,
            previewModalTitle: '',
            previewHtml: '',
            previewMode: 'list' // 'list' | 'detail'
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
        
        // View inspection — load details + open same print preview modal as Print
        async viewInspectionDetails(inspectionId) {
            try {
                if (!this.isValidUUID(inspectionId)) {
                    console.warn('Invalid UUID:', inspectionId);
                    this.showAlert('Invalid inspection ID format', 'danger');
                    return;
                }

                let detail = null;
                try {
                    const fnUrl = `${this.apiUrl}/getInspectionDetails(inspectionId='${inspectionId}')`;
                    const response = await axios.get(fnUrl);
                    detail = response.data.value !== undefined ? response.data.value : response.data;
                } catch (e1) {
                    console.warn('getInspectionDetails failed, falling back to Inspection entity:', e1);
                    const response = await axios.get(`${this.apiUrl}/Inspection(ID=${inspectionId})`);
                    const row = response.data;
                    const eqId = row.equipment_ID || row.equipmentId;
                    const inId = row.inspector_ID || row.inspectorId;
                    const eq = this.equipment.find((x) => x.ID === eqId);
                    const ins = this.inspectors.find((x) => x.ID === inId);
                    detail = {
                        inspectionId: row.ID,
                        equipmentName: eq ? eq.name : '',
                        equipmentType: eq ? eq.type : '',
                        equipmentLocation: eq ? eq.location : '',
                        serialNumber: eq ? eq.serialNumber : '',
                        inspectionDate: row.inspectionDate,
                        completionDate: row.completionDate,
                        inspectorName: ins ? ins.name : '',
                        inspectorDepartment: ins ? ins.department : '',
                        status: row.status,
                        findings: row.findings,
                        safetyIssues: row.safetyIssues,
                        notes: row.notes
                    };
                }

                this.previewMode = 'detail';
                this.previewModalTitle = 'Inspection — preview';
                this.previewHtml = this.buildDetailReportHtml(detail);
                this.previewModalOpen = true;
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
        
        /** Open HTML print preview for current search results (same UX as demo.html modal) */
        openListPrintPreview() {
            if (this.inspections.length === 0) {
                this.showAlert('No inspections to print', 'warning');
                return;
            }
            this.previewMode = 'list';
            this.previewModalTitle = 'Inspection list — print preview';
            this.previewHtml = this.buildListReportHtml();
            this.previewModalOpen = true;
        },

        closePreviewModal() {
            this.previewModalOpen = false;
            this.previewHtml = '';
        },

        printPreviewContent() {
            this.$nextTick(() => {
                window.print();
            });
        },

        /** Server-side PDF for current list (optional; preview modal also has Print for browser) */
        async downloadListPdf() {
            try {
                if (this.inspections.length === 0) {
                    this.showAlert('No inspections to print', 'warning');
                    return;
                }

                const inspectionIds = this.inspections.map((i) => i.ID);
                const response = await axios.post(`${this.apiUrl}/printInspections`, {
                    inspectionIds: inspectionIds
                });

                let pdfBase64 = response.data;
                if (response.data.value) {
                    pdfBase64 = response.data.value;
                }

                const binaryString = atob(pdfBase64);
                const bytes = new Uint8Array(binaryString.length);
                for (let i = 0; i < binaryString.length; i++) {
                    bytes[i] = binaryString.charCodeAt(i);
                }

                const blob = new Blob([bytes], { type: 'application/pdf' });
                const url = URL.createObjectURL(blob);
                window.open(url);

                this.showAlert('PDF opened in new window', 'success');
            } catch (error) {
                console.error('Error generating PDF:', error);
                this.showAlert('Failed to generate PDF', 'danger');
            }
        },

        escapeHtml(str) {
            if (str == null || str === '') {
                return '';
            }
            return String(str)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;');
        },

        /** Safe for v-html: escaped, newlines as <br> */
        escapeHtmlMultiline(str) {
            return this.escapeHtml(str || '').replace(/\n/g, '<br>');
        },

        formatDateTime() {
            const now = new Date();
            return now.toLocaleString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        },

        buildListReportHtml() {
            const rows = this.inspections
                .map((row, idx) => {
                    const findings = row.findings
                        ? this.escapeHtml(row.findings.length > 80 ? row.findings.substring(0, 80) + '…' : row.findings)
                        : '—';
                    return `<tr>
                        <td>${idx + 1}</td>
                        <td>${this.escapeHtml(String(row.ID || ''))}</td>
                        <td>${this.escapeHtml(String(row.equipmentName || '—'))}</td>
                        <td>${this.escapeHtml(String(row.inspectorName || '—'))}</td>
                        <td>${this.escapeHtml(String(this.formatDate(row.inspectionDate)))}</td>
                        <td>${this.escapeHtml(String(row.status || '—'))}</td>
                        <td>${findings}</td>
                    </tr>`;
                })
                .join('');

            return `
                <div class="preview-report-header">
                    <div class="preview-report-title">Inspection list report</div>
                    <div class="preview-report-meta">Generated: ${this.escapeHtml(this.formatDateTime())}</div>
                </div>
                <div class="preview-report-summary">
                    <strong>Records:</strong> ${this.inspections.length} · <strong>Source:</strong> Equipment Inspection System (preview before print)
                </div>
                <div style="overflow-x:auto;">
                    <table class="preview-data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>ID</th>
                                <th>Equipment</th>
                                <th>Inspector</th>
                                <th>Date</th>
                                <th>Status</th>
                                <th>Findings</th>
                            </tr>
                        </thead>
                        <tbody>${rows}</tbody>
                    </table>
                </div>
                <div class="preview-footer-note">Preview — use Print in the dialog header, or download PDF for server-generated file.</div>
            `;
        },

        buildDetailReportHtml(d) {
            const id = this.escapeHtml(d.inspectionId || d.ID || '');
            const dateStr = this.escapeHtml(this.formatDate(d.inspectionDate));
            const compStr = d.completionDate
                ? this.escapeHtml(this.formatDate(d.completionDate))
                : '—';
            const findings = d.findings ? this.escapeHtmlMultiline(d.findings) : '—';
            const safety = d.safetyIssues ? this.escapeHtmlMultiline(d.safetyIssues) : '—';
            const notes = d.notes ? this.escapeHtmlMultiline(d.notes) : '—';
            return `
                <div class="preview-report-header">
                    <div class="preview-report-title">Inspection report</div>
                    <div class="preview-report-meta">Generated: ${this.escapeHtml(this.formatDateTime())}</div>
                </div>
                <div class="preview-report-summary">
                    <strong>Inspection ID:</strong> ${id} · <strong>Status:</strong> ${this.escapeHtml(String(d.status || '—'))}
                </div>
                <dl class="preview-detail-grid">
                    <div class="preview-detail-item">
                        <dt>Equipment</dt>
                        <dd>${this.escapeHtml(String(d.equipmentName || '—'))}</dd>
                    </div>
                    <div class="preview-detail-item">
                        <dt>Type</dt>
                        <dd>${this.escapeHtml(String(d.equipmentType || '—'))}</dd>
                    </div>
                    <div class="preview-detail-item">
                        <dt>Location</dt>
                        <dd>${this.escapeHtml(String(d.equipmentLocation || '—'))}</dd>
                    </div>
                    <div class="preview-detail-item">
                        <dt>Serial</dt>
                        <dd>${this.escapeHtml(String(d.serialNumber || '—'))}</dd>
                    </div>
                    <div class="preview-detail-item">
                        <dt>Inspector</dt>
                        <dd>${this.escapeHtml(String(d.inspectorName || '—'))}</dd>
                    </div>
                    <div class="preview-detail-item">
                        <dt>Department</dt>
                        <dd>${this.escapeHtml(String(d.inspectorDepartment || '—'))}</dd>
                    </div>
                    <div class="preview-detail-item">
                        <dt>Inspection date</dt>
                        <dd>${dateStr}</dd>
                    </div>
                    <div class="preview-detail-item">
                        <dt>Completion date</dt>
                        <dd>${compStr}</dd>
                    </div>
                </dl>
                <div class="mb-3">
                    <div class="preview-field-label">Findings</div>
                    <div class="preview-block-text">${findings}</div>
                </div>
                <div class="mb-3">
                    <div class="preview-field-label">Safety issues</div>
                    <div class="preview-block-text">${safety}</div>
                </div>
                <div class="mb-3">
                    <div class="preview-field-label">Notes</div>
                    <div class="preview-block-text">${notes}</div>
                </div>
                <div class="preview-footer-note">Use Print in the dialog header to print this inspection.</div>
            `;
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
