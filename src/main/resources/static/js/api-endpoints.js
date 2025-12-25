// make feature cards clickable
document.querySelectorAll('.feature-card').forEach(card => {
    card.style.cursor = 'pointer';
});

// adds copy functionality for endpoint paths
document.querySelectorAll('.copy-endpoint').forEach(element => {
    element.addEventListener('click', function() {
        const textToCopy = this.textContent;
        navigator.clipboard.writeText(textToCopy)
            .then(() => {
                // shows temporary feedback
                const originalText = this.textContent;
                this.textContent = 'Copied!';
                setTimeout(() => {
                    this.textContent = originalText;
                }, 1500);
            })
            .catch(err => {
                console.error('Failed to copy: ', err);
            });
    });
});

// sets method badge colors based on HTTP method
document.querySelectorAll('.method').forEach(badge => {
    const method = badge.textContent.trim().toLowerCase();
    switch(method) {
        case 'get':
            badge.style.backgroundColor = '#28a745';
            break;
        case 'post':
            badge.style.backgroundColor = '#007bff';
            break;
        case 'put':
            badge.style.backgroundColor = '#fd7e14';
            break;
        case 'delete':
            badge.style.backgroundColor = '#dc3545';
            break;
        default:
            badge.style.backgroundColor = '#6c757d';
    }
    badge.style.color = 'white';
    badge.style.padding = '0.25rem 0.75rem';
    badge.style.borderRadius = '0.375rem';
    badge.style.fontSize = '0.875rem';
    badge.style.fontWeight = '600';
    badge.style.marginRight = '0.75rem';
});