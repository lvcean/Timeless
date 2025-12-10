const SUPABASE_URL = 'https://mwmmknlbeokrldsybsje.supabase.co';
const SUPABASE_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im13bW1rbmxiZW9rcmxkc3lic2plIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjUxODE1MTEsImV4cCI6MjA4MDc1NzUxMX0.HxoClrQpexO1nvXTNJ5OvYhHyWCKl6S7x1sEMHZjMCg';
const client = supabase.createClient(SUPABASE_URL, SUPABASE_KEY);

let currentUser = null;
let selectedColor = '#52B788'; // Default Mint Green
let selectedIconKey = 'Outlined.EditCalendar';

// --- ANDROID ICON MAPPINGS ---
const ICON_DB = [
    { key: "Outlined.EditCalendar", name: "edit_calendar" },
    { key: "Outlined.DirectionsRun", name: "directions_run" },
    { key: "Outlined.FitnessCenter", name: "fitness_center" },
    { key: "Outlined.Work", name: "work" },
    { key: "Outlined.MenuBook", name: "menu_book" },
    { key: "Outlined.LocalCafe", name: "local_cafe" },
    { key: "Outlined.Bed", name: "bed" },
    { key: "Outlined.ShoppingCart", name: "shopping_cart" },
    { key: "Outlined.MusicNote", name: "music_note" },
    { key: "Outlined.AttachMoney", name: "attach_money" },
    { key: "Outlined.SelfImprovement", name: "self_improvement" },
    { key: "Outlined.Pool", name: "pool" },
    { key: "Outlined.Computer", name: "computer" },
    { key: "Outlined.Code", name: "code" },
    { key: "Outlined.School", name: "school" },
    { key: "Outlined.Email", name: "email" },
    { key: "Outlined.Home", name: "home" },
    { key: "Outlined.Movie", name: "movie" },
    { key: "Outlined.Pets", name: "pets" },
    { key: "Outlined.Flight", name: "flight" }
];

// Helper: Convert Android Key to HTML
function getIconHtml(dbIconKey) {
    const match = ICON_DB.find(i => i.key === dbIconKey);
    if (match) return `<span class="material-icons-outlined">${match.name}</span>`;

    // Fallback/Guessing
    if (!dbIconKey || dbIconKey.length > 30) return '<span class="material-icons-outlined">event</span>';
    if (dbIconKey.includes('.')) {
        const guess = dbIconKey.split('.')[1].replace(/([a-z])([A-Z])/g, '$1_$2').toLowerCase();
        return `<span class="material-icons-outlined">${guess}</span>`;
    }
    return dbIconKey;
}

// --- INIT ---
async function init() {
    renderIconSelector();
    // Check if user is already logged in
    const { data: { session } } = await client.auth.getSession();
    if (session) {
        showApp(session.user);
    } else {
        showLandingPage();
    }
}
window.onload = init;

// --- NAVIGATION ---
function showLandingPage() {
    document.getElementById('landing-page').style.display = 'block';
    document.getElementById('web-app-container').style.display = 'none';
    document.getElementById('login-screen').style.display = 'none';
}

function openLogin() {
    document.getElementById('login-screen').style.display = 'flex';
}

function closeLogin() {
    document.getElementById('login-screen').style.display = 'none';
}

function showApp(user) {
    currentUser = user;
    document.getElementById('landing-page').style.display = 'none';
    document.getElementById('login-screen').style.display = 'none';
    document.getElementById('web-app-container').style.display = 'block';
    document.getElementById('current-user').innerText = user.email;
    fetchEvents();
    setupRealtime();
}

// --- AUTH ---
async function sendOtp() {
    const email = document.getElementById('email-input').value;
    if (!email) return showMsg('请输入邮箱');
    setLoading(true, '发送中...');
    const { error } = await client.auth.signInWithOtp({ email });
    setLoading(false);
    if (error) showMsg(error.message);
    else {
        document.getElementById('step-email').style.display = 'none';
        document.getElementById('step-code').style.display = 'block';
        showMsg('验证码已发送');
    }
}

async function verifyOtp() {
    const email = document.getElementById('email-input').value;
    const token = document.getElementById('code-input').value;
    if (token.length < 4) return showMsg('请输入验证码');
    setLoading(true, '验证中...');
    const { data, error } = await client.auth.verifyOtp({ email, token, type: 'email' });
    setLoading(false);
    if (error) showMsg(error.message);
    else showApp(data.user);
}

async function logout() {
    await client.auth.signOut();
    location.reload();
}

function resetLogin() {
    document.getElementById('step-code').style.display = 'none';
    document.getElementById('step-email').style.display = 'block';
    showMsg('');
}

// --- APP LOGIC ---
async function fetchEvents() {
    if (!currentUser) return;
    const { data, error } = await client.from('events')
        .select('*')
        .eq('creator_id', currentUser.id)
        .order('created_at', { ascending: false });

    if (error) return console.error(error);
    renderEvents(data);
}

function renderEvents(events) {
    const grid = document.getElementById('event-grid');
    grid.innerHTML = '';

    if (events.length === 0) {
        grid.innerHTML = '<div style="text-align:center; grid-column:1/-1; padding:50px; color:var(--text-secondary)">✨ Empty timeline. Create your first event!</div>';
        return;
    }

    events.forEach((event, index) => {
        // Color Parsing with Fallback
        let bgColor = '#52B788';
        if (event.background_color) {
            if (event.background_color.toString().startsWith('#')) bgColor = event.background_color;
            else {
                const c = parseInt(event.background_color);
                if (!isNaN(c)) bgColor = '#' + (c & 0x00FFFFFF).toString(16).padStart(6, '0');
            }
        }

        // Determine Icon Color based on Background Brightness
        const isLight = isLightColor(bgColor);
        const iconColor = isLight ? '#1A1C1A' : '#FFFFFF';

        const iconHtml = getIconHtml(event.icon);
        const card = document.createElement('div');
        card.className = 'card';
        card.style.animationDelay = `${index * 0.05}s`;

        // Inline style for specific card color (border/shadow accent)
        card.style.borderLeft = `4px solid ${bgColor}`;

        card.innerHTML = `
            <button class="delete-btn" onclick="deleteEvent('${event.id}')" title="删除">×</button>
            <div class="card-header">
                <div class="icon-box" style="background-color: ${bgColor}; color: ${iconColor}; box-shadow: 0 4px 10px ${bgColor}40">
                    ${iconHtml}
                </div>
                <div class="text-container">
                    <div class="event-name" title="${event.name}">${event.name}</div>
                    <div class="event-category">${event.category || 'DAILY'}</div>
                </div>
            </div>
        `;
        grid.appendChild(card);
    });
}

// Helper: Check brightness (YIQ equation)
function isLightColor(color) {
    const hex = color.replace('#', '');
    const r = parseInt(hex.substr(0, 2), 16);
    const g = parseInt(hex.substr(2, 2), 16);
    const b = parseInt(hex.substr(4, 2), 16);
    const yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000;
    return yiq >= 128;
}

// Create Event
async function submitNewEvent() {
    const name = document.getElementById('new-name').value;
    if (!name) return alert('请先输入名称');

    const newEvent = {
        name: name,
        icon: selectedIconKey,
        background_color: selectedColor,
        category: 'DAILY', // Fixed Category
        creator_id: currentUser.id
        // Removed: is_custom, is_quick_record, event_type, group_name due to Schema Mismatch
    };

    const { error } = await client.from('events').insert([newEvent]);

    if (error) {
        console.error(error);
        alert(`Failed: ${error.message} (${error.code})`);
    } else {
        closeModal();
        fetchEvents();
    }
}

async function deleteEvent(id) {
    if (!confirm('Delete this event?')) return;
    const { error } = await client.from('events').delete().eq('id', id);
    if (error) alert(error.message); else fetchEvents();
}

function setupRealtime() {
    client.channel('public:events')
        .on('postgres_changes', { event: '*', schema: 'public', table: 'events' }, payload => {
            const record = payload.new || payload.old;
            if (record && record.creator_id === currentUser.id) fetchEvents();
        })
        .subscribe();
}

// --- UI HELPERS ---
function renderIconSelector() {
    const container = document.getElementById('icon-selector');
    container.innerHTML = '';
    ICON_DB.forEach((icon, index) => {
        const div = document.createElement('div');
        div.className = `icon-option ${index === 0 ? 'active' : ''}`;
        div.innerHTML = `<span class="material-icons-outlined">${icon.name}</span>`;
        div.onclick = () => {
            document.querySelectorAll('.icon-option').forEach(el => el.classList.remove('active'));
            div.classList.add('active');
            selectedIconKey = icon.key;
        };
        container.appendChild(div);
    });
}

function showMsg(text) { document.getElementById('login-msg').innerText = text; }
function setLoading(isLoading, text) {
    const btns = document.querySelectorAll('.login-box button.btn-primary');
    btns.forEach(btn => {
        if (btn.id !== 'submit-event-btn') { // Don't change the create button
            btn.innerText = isLoading ? text : (btn.id === 'send-btn' ? '获取验证码' : '即刻进入');
            btn.disabled = isLoading;
        }
    });
}

function openAddModal() { document.getElementById('modal').style.display = 'flex'; }
function closeModal() { document.getElementById('modal').style.display = 'none'; }

function selectColor(color, el) {
    selectedColor = color;
    document.querySelectorAll('.color-dot').forEach(d => {
        d.classList.remove('active');
        d.style.borderColor = 'transparent'; // Reset border
    });
    el.classList.add('active');
    el.style.borderColor = '#333'; // Highlight selection
}

// --- ANIMATION OBSERVER ---
const observerOptions = { threshold: 0.1 };
const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.classList.add('visible');
            observer.unobserve(entry.target); // Only animate once
        }
    });
}, observerOptions);

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.scroll-reveal').forEach(el => observer.observe(el));
});
