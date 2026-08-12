// --- Router ---
function router() {
  const path = window.location.hash.slice(1) || "/catalog";
  if (path.startsWith("/cake/")) {
    const id = path.split("/")[2];
    renderCake(id);
  } else if (path === "/catalog") {
    renderCatalog();
  } else if (path === "/summary") {
    renderSummary();
  } else if (path === "/admin") {
    renderAdmin();
  } else if (path === "/notifications") {
    renderNotifications();
  } else if (path === "/checkout") {
    renderCheckout();
  }
}

window.addEventListener("hashchange", router);
window.addEventListener("load", router);
function getUserId() {
  let userId = localStorage.getItem("userId");
  if (!userId) {
    userId = prompt("Enter your username / user ID:");
    if (!userId || !userId.trim()) {
      userId = "guest" + Math.floor(Math.random() * 100000);
    }
    localStorage.setItem("userId", userId.trim());
  }
  return userId;
}

// --- Catalog View ---
async function renderCatalog() {
  const res = await fetch("/cakecatalog/getallcake");
  const cakes = await res.json();
  document.getElementById("app").innerHTML = `
    <div class="container">
      <div class="sidebar">
        <h3>Filters</h3>
        <input id="filterName" placeholder="Search by name">
        <input id="filterCategory" placeholder="Category">
        <input id="minPrice" type="number" placeholder="₹ Min">
        <input id="maxPrice" type="number" placeholder="₹ Max">
        <button onclick="applyFilter()">Apply Filter</button>
      </div>
      <div class="content" id="cake-list">
        ${cakes.map(cake => `
          <div class="cake-card" onclick="navigateTo('/cake/${cake.id}')">
            <img src="${cake.imageUrl}" alt="${cake.name}">
            <h3>${cake.name}</h3>
            <p>Category: ${cake.category}</p>
            <p>Price: ₹${cake.price}</p>
          </div>
        `).join("")}
      </div>
    </div>
  `;
}

async function applyFilter() {
  const params = new URLSearchParams();
  if (document.getElementById("filterName").value) params.append("name", document.getElementById("filterName").value);
  if (document.getElementById("filterCategory").value) params.append("category", document.getElementById("filterCategory").value);
  if (document.getElementById("minPrice").value) params.append("minPrice", document.getElementById("minPrice").value);
  if (document.getElementById("maxPrice").value) params.append("maxPrice", document.getElementById("maxPrice").value);

  const res = await fetch(`/cakecatalog/filter?${params.toString()}`);
  const data = await res.json();
  renderCakes(data);
}

function renderCakes(data) {
  const list = document.getElementById("cake-list");
  list.innerHTML = data.map(cake => `
    <div class="cake-card" onclick="navigateTo('/cake/${cake.id}')">
      <img src="${cake.imageUrl}" alt="${cake.name}">
      <h3>${cake.name}</h3>
      <p>Category: ${cake.category}</p>
      <p>Price: ₹${cake.price}</p>
    </div>
  `).join("");
}


// --- Cake Details View ---
async function renderCake(id) {
  const res = await fetch(`/cakecatalog/getcake/${id}`);
  const cake = await res.json();
  document.getElementById("app").innerHTML = `
    <div class="container">
      <div class="cake-image"><img src="${cake.imageUrl}" alt="${cake.name}"></div>
      <div class="cake-info">
        <h2>${cake.name}</h2>
        <p><strong>Category:</strong> ${cake.category}</p>
        <p class="price">₹${cake.price}</p>
        <p><strong>Description:</strong> ${cake.description || "No description available."}</p>
        <div class="buttons">
          <button onclick="addToCart(${cake.id})">Add to Cart</button>
          <button onclick="buyNow(${cake.id})">Buy Now</button>
        </div>

        <div class="rating-section" id="rating-section">
          <h3>Ratings</h3>
          <p id="average-rating">Loading average rating...</p>
          <div class="star-input" id="star-input">
            ${[1,2,3,4,5].map(n => `<span class="star" data-value="${n}" onclick="selectStar(${n})">&#9733;</span>`).join("")}
          </div>
          <button onclick="submitRating(${cake.id})">Submit Rating</button>
        </div>
      </div>
    </div>
  `;
  loadRatingInfo(id);
}

let selectedRating = 0;

function selectStar(value) {
  selectedRating = value;
  document.querySelectorAll("#star-input .star").forEach(star => {
    star.classList.toggle("selected", Number(star.dataset.value) <= value);
  });
}

async function loadRatingInfo(cakeId) {
  try {
    const res = await fetch(`/rating/average/${cakeId}`);
    const avg = await res.json();
    document.getElementById("average-rating").textContent =
      avg > 0 ? `Average Rating: ${avg.toFixed(1)} / 5` : "No ratings yet";
  } catch (err) {
    document.getElementById("average-rating").textContent = "Unable to load rating";
  }
}

async function submitRating(cakeId) {
  const userId = getUserId();
  if (selectedRating === 0) {
    alert("Please select a star rating first.");
    return;
  }

  try {
    const res = await fetch("/rating/submitRating", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, cakeId, rating: selectedRating })
    });

    if (!res.ok) throw new Error("Failed to submit rating");

    alert("Thanks for rating!");
    selectedRating = 0;
    document.querySelectorAll("#star-input .star").forEach(s => s.classList.remove("selected"));
    loadRatingInfo(cakeId);
  } catch (err) {
    alert("Error submitting rating: " + err.message);
  }
}
//basket summary
async function renderSummary() {
  const userId = getUserId();
  const res = await fetch(`/order/basket/summary/${userId}`);
  const data = await res.json();
  document.getElementById("app").innerHTML = `
    <div class="basket-container">
      <h2>Your Basket</h2>
      <div id="basket-list">
        ${data.items.map(item => `
          <div class="basket-item">
            <img src="${item.imageUrl}" alt="${item.cakeName}">
            <div class="basket-details">
              <h3>${item.cakeName}</h3>
              <p>Qty: ${item.quantity}</p>
              <p>Price: ₹${item.price}</p>
              <p>Subtotal: ₹${item.price * item.quantity}</p>
            </div>
            <button class="delete-btn" onclick="deleteItem(${item.id})">Delete</button>
          </div>
        `).join("")}
      </div>
      <div class="total">Total: ₹${data.total}</div>
      <button class="checkout-btn" onclick="checkout()">Checkout</button>
    </div>
  `;
}

async function deleteItem(id) {
  await fetch(`/order/basket/deleteBasket/${id}`, { method: "DELETE" });
  alert("Item deleted!");
  renderSummary();
}


async function checkout() {
  const userId = getUserId();
  const address = prompt("Enter your delivery address:");
  if (!address) return;
  const email = prompt("Enter your email for order confirmation:");
  if (!email) return;

  try {
    const res = await fetch(`/order/basket/checkout/${userId}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, address })
    });

    if (!res.ok) throw new Error("Checkout failed");
    const order = await res.json();
    alert("Order placed! Order ID: " + order.id + "\nConfirmation email sent to " + email);
    navigateTo("/notifications");
  } catch (err) {
    alert("Error placing order: " + err.message);
  }
}

// --- Admin Panel View (Add New Cake form) ---
function renderAdmin() {
  document.getElementById("app").innerHTML = `
    <div class="admin-form-container">
      <h2>Add New Cake</h2>

      <label>Cake Name</label>
      <input id="cakeName" type="text">

      <label>Category</label>
      <input id="cakeCategory" type="text">

      <label>Price</label>
      <input id="cakePrice" type="number">

      <label>Image URL</label>
      <input id="cakeImageUrl" type="text">

      <label>Description</label>
      <textarea id="cakeDescription"></textarea>

      <label>Availability</label>
      <select id="cakeAvailability">
        <option value="true">Available</option>
        <option value="false">Not Available</option>
      </select>

      <button onclick="addCake()">Add Cake</button>
    </div>
  `;
}

async function addCake() {
  const newCake = {
    name: document.getElementById("cakeName").value,
    category: document.getElementById("cakeCategory").value,
    price: parseFloat(document.getElementById("cakePrice").value),
    imageUrl: document.getElementById("cakeImageUrl").value,
    description: document.getElementById("cakeDescription").value,
    available: document.getElementById("cakeAvailability").value === "true"
  };

  if (!newCake.name || !newCake.category || isNaN(newCake.price)) {
    alert("Please fill in Name, Category, and Price.");
    return;
  }

  try {
    const res = await fetch("/cakecatalog/addcake", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(newCake)
    });

    if (!res.ok) throw new Error("Failed to add cake");

    alert("Cake added successfully!");
    navigateTo("/catalog");
  } catch (err) {
    alert("Error adding cake: " + err.message);
  }
}

// --- Notifications View ---

  // --- Notifications View ---
  async function renderNotifications() {
    const userId = getUserId();
    const res = await fetch(`/notification/${userId}`);
    const notifications = await res.json();
    document.getElementById("app").innerHTML = `
      <div class="notifications-container">
        <h2>Your Notifications</h2>
        <div id="notification-list">
          ${notifications.length ? notifications.slice().reverse().map(n => `
            <div class="notification-card">
              <div class="notif-content">
                <p class="notif-message">${n.message}</p>
                <p class="status">${n.status} · ${new Date(n.sentAt).toLocaleString()}</p>
              </div>
              <button class="notif-delete-btn" onclick="deleteNotification(${n.id})">Delete</button>
            </div>
          `).join("") : `<p class="empty-notif">No notifications yet.</p>`}
        </div>
      </div>
    `;
  }

  async function deleteNotification(id) {
    try {
      await fetch(`/notification/${id}`, { method: "DELETE" });
      renderNotifications();
    } catch (err) {
      alert("Failed to delete notification: " + err.message);
    }
  }

// --- Checkout View (optional separate page) ---
function renderCheckout() {
  document.getElementById("app").innerHTML = `
    <div class="container">
      <h2>Checkout</h2>
      <p>Proceed with payment (future integration).</p>
    </div>
  `;
}

// --- Helpers ---
function navigateTo(path) { window.location.hash = path; }
async function toggleNotifications() {
  const panel = document.getElementById("notifications");
  const isShowing = panel.classList.contains("show");

  if (!isShowing) {
    await loadNotificationPanel();
  }
  panel.classList.toggle("show");
}

async function loadNotificationPanel() {
  const userId = getUserId();
  const panel = document.getElementById("notifications");
  try {
    const res = await fetch(`/notification/${userId}`);
    const notifications = await res.json();

    if (!notifications.length) {
      panel.innerHTML = `<p style="margin:0;color:#777;">No notifications yet.</p>`;
      return;
    }

    panel.innerHTML = notifications
      .slice()
      .reverse()
      .map(n => `
        <div class="notif-item" style="padding:8px 0;border-bottom:1px solid #eee;">
          <p style="margin:0;font-size:13px;white-space:pre-line;">${n.message}</p>
          <p style="margin:4px 0 0;font-size:11px;color:#999;">
            ${n.status} · ${new Date(n.sentAt).toLocaleString()}
          </p>
        </div>
      `).join("");
  } catch (err) {
    console.error("Failed to load notifications:", err);
    panel.innerHTML = `<p style="margin:0;color:#c00;">Failed to load notifications.</p>`;
  }
}

async function addToCart(cakeId) {
  const userId = getUserId();
  await fetch(`/order/basket/addBasket?userId=${userId}&cakeId=${cakeId}&quantity=1`, { method: "POST" });
  alert("Cake added to basket!");
  navigateTo("/summary");
}

async function buyNow(cakeId) {
  const userId = getUserId();
  await fetch(`/order/basket/addBasket?userId=${userId}&cakeId=${cakeId}&quantity=1`, { method: "POST" });
  navigateTo("/checkout");
}