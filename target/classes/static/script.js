document.addEventListener('DOMContentLoaded', () => {
    const requestsCtx = document.getElementById('requestsChart').getContext('2d');
    const bytesCtx = document.getElementById('bytesChart').getContext('2d');

    let requestsChart;
    let bytesChart;

    let lastMetrics = {
        'requests.total': 0,
        'bytes.total': 0,
        'timestamp': Date.now()
    };

    function createChart(ctx, title, label, borderColor) {
        return new Chart(ctx, {
            type: 'line',
             { // Added 'data' object
                labels: [], // Timestamps or time strings (used for x-axis in linear scale)
                datasets: [{
                    label: label,
                     [], // Added 'data' key - This holds the actual metric values
                    borderColor: borderColor,
                    borderWidth: 1,
                    fill: false,
                    tension: 0.1,
                    pointRadius: 0 // Don't show points
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    x: {
                        type: 'linear', // Use linear scale for timestamps
                        title: {
                            display: true,
                            text: 'Time (seconds since start)'
                        },
                        ticks: {
                             callback: function(value, index, values) {
                                // Display time relative to the start
                                // Use the first timestamp in the data as the start time reference
                                const startTime = requestsChart && requestsChart.data.labels.length > 0 ? requestsChart.data.labels[0] : Date.now();
                                return ((value - startTime) / 1000).toFixed(0);
                             }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: label
                        }
                    }
                },
                plugins: {
                    title: {
                        display: true,
                        text: title
                    },
                    tooltip: {
                         callbacks: {
                             title: function(tooltipItems) {
                                 const item = tooltipItems[0];
                                 const timestamp = item.parsed.x;
                                 // Use the first timestamp in the data as the start time reference
                                 const startTime = requestsChart && requestsChart.data.labels.length > 0 ? requestsChart.data.labels[0] : Date.now();
                                 const secondsElapsed = ((timestamp - startTime) / 1000).toFixed(1);
                                 return `Time: ${secondsElapsed}s`;
                             },
                             label: function(tooltipItem) {
                                 return `${tooltipItem.dataset.label}: ${tooltipItem.parsed.y.toFixed(2)}`;
                             }
                         }
                    }
                }
            }
        });
    }

    // Initialize charts
    requestsChart = createChart(requestsCtx, 'Requests Per Second', 'Req/s', 'rgb(75, 192, 192)');
    bytesChart = createChart(bytesCtx, 'Bytes Per Second', 'Bytes/s', 'rgb(255, 99, 132)');

    function fetchDataAndUpdateCharts() {
        fetch('/metrics/current')
            .then(response => response.json())
            .then(currentMetrics => {
                const currentTime = currentMetrics.timestamp;
                const timeDelta = (currentTime - lastMetrics.timestamp) / 1000; // Delta in seconds

                if (timeDelta <= 0) {
                    // Avoid division by zero or negative time delta
                    console.warn("Time delta is zero or negative, skipping update.");
                    return;
                }

                // Calculate rates
                const requestsDelta = currentMetrics['requests.total'] - lastMetrics['requests.total'];
                const bytesDelta = currentMetrics['bytes.total'] - lastMetrics['bytes.total'];

                const requestsPerSecond = requestsDelta / timeDelta;
                const bytesPerSecond = bytesDelta / timeDelta;

                // Add data points to charts
                const timeLabel = currentTime; // Use timestamp as the x-axis value

                requestsChart.data.labels.push(timeLabel);
                requestsChart.data.datasets[0].data.push(requestsPerSecond);

                bytesChart.data.labels.push(timeLabel);
                bytesChart.data.datasets[0].data.push(bytesPerSecond);

                // Keep the number of data points manageable (optional)
                const maxDataPoints = 100; // Show last 100 seconds/intervals
                if (requestsChart.data.labels.length > maxDataPoints) {
                    requestsChart.data.labels.shift();
                    requestsChart.data.datasets[0].data.shift();
                    bytesChart.data.labels.shift();
                    bytesChart.data.datasets[0].data.shift();
                }


                // Update charts
                requestsChart.update();
                bytesChart.update();

                // Store current metrics for the next calculation
                lastMetrics = currentMetrics;

            })
            .catch(error => {
                console.error('Error fetching metrics:', error);
            });
    }

    // Poll for data every 1 second
    const pollingInterval = 1000; // milliseconds
    setInterval(fetchDataAndUpdateCharts, pollingInterval);

    // Fetch initial data immediately
    fetchDataAndUpdateCharts();
});
