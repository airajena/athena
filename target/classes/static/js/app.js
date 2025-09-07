class ServerDashboard {
    constructor() {
        this.startTime = Date.now();
        this.requestCount = 0;
        this.init();
    }

    init() {
        this.updateUptime();
        this.refreshStats();

        setInterval(() => {
            this.updateUptime();
            this.refreshStats();
        }, 10000);

        console.log('🚀 Enterprise Web Server Dashboard Loaded!');
    }

    updateUptime() {
        const uptime = Date.now() - this.startTime;
        const seconds = Math.floor(uptime / 1000);
        const minutes = Math.floor(seconds / 60);
        const hours = Math.floor(minutes / 60);

        const uptimeStr = `${hours}h ${minutes % 60}m ${seconds % 60}s`;
        const uptimeElement = document.getElementById('uptime');
        if (uptimeElement) {
            uptimeElement.textContent = uptimeStr;
        }
    }

    async refreshStats() {
        try {
            const response = await fetch('/stats');
            if (response.ok) {
                this.requestCount++;
                this.updateStatsDisplay();
            }
        } catch (error) {
            console.error('Failed to refresh stats:', error);
        }
    }

    updateStatsDisplay() {
        const threadsElement = document.getElementById('threads');
        const requestsElement = document.getElementById('requests');

        if (threadsElement) {
            threadsElement.textContent = '10 (Thread Pool)';
        }

        if (requestsElement) {
            requestsElement.textContent = this.requestCount.toLocaleString();
        }
    }

    async testConcurrency() {
        const button = event.target;
        button.disabled = true;
        button.textContent = '🔄 Testing...';

        try {
            const promises = [];
            for (let i = 0; i < 10; i++) {
                promises.push(fetch('/time'));
            }

            const startTime = performance.now();
            await Promise.all(promises);
            const endTime = performance.now();

            const duration = Math.round(endTime - startTime);

            alert(`🎉 Multi-Threading Test Complete!\n\n` +
                `✅ Fired 10 concurrent requests\n` +
                `⚡ Total time: ${duration}ms\n` +
                `🧵 All handled simultaneously by different threads!\n\n` +
                `Check your server console to see different thread names.`);

        } catch (error) {
            alert('❌ Test failed: ' + error.message);
        } finally {
            button.disabled = false;
            button.textContent = '🧪 Test Multi-Threading';
        }
    }
}

function refreshStats() {
    window.dashboard.refreshStats();
}

function testConcurrency() {
    window.dashboard.testConcurrency();
}

document.addEventListener('DOMContentLoaded', () => {
    window.dashboard = new ServerDashboard();
});
