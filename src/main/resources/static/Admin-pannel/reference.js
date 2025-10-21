document.addEventListener('DOMContentLoaded', function() {
    function loadHTML(url, elementId, callback) {
        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
                return response.text();
            })
            .then(html => {
                const targetElement = document.getElementById(elementId);
                if (targetElement) {
                    targetElement.innerHTML = html;
                    if (callback) callback();
                }
            })
            .catch(error => console.error(`Could not load ${url}:`, error));
    }

    function setActiveSidebarLink() {
        const currentPath = window.location.pathname;
        const sidebarLinks = document.querySelectorAll('.sidebar-menu a');

        sidebarLinks.forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') !== '#') {
                const linkPath = new URL(link.href).pathname;
                if (linkPath === currentPath || 
                    (linkPath === '/index.html' && currentPath === '/')) {
                    link.classList.add('active');
                }
            }
        });
    }

    // Load Sidebar + Topnav
    loadHTML('sidebar.html', 'sidebar-placeholder', setActiveSidebarLink);
    loadHTML('topnav.html', 'topnav-placeholder');
});
