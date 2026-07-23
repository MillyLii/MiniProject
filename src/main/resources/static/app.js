const params = new URLSearchParams(window.location.search);
const employeeName = (params.get("employee") || "").trim();

if (!employeeName) {
    window.location.replace("/");
}

const dateInput = document.getElementById("date");
const floorSelect = document.getElementById("floor");
const monitorSelect = document.getElementById("monitor");

const deskList = document.getElementById("deskList");
const message = document.getElementById("message");

const bookingForm = document.getElementById("bookingForm");
const deskIdInput = document.getElementById("deskId");
const selectedDeskCodeInput = document.getElementById("selectedDeskCode");
const employeeNameInput = document.getElementById("employeeName");
const bookingDateInput = document.getElementById("bookingDate");
const bookingMessage = document.getElementById("bookingMessage");

const bookingList = document.getElementById("bookingList");
const bookingListMessage = document.getElementById("bookingListMessage");
const weekBookingList = document.getElementById("weekBookingList");
const weekMessage = document.getElementById("weekMessage");
const weekRangeLabel = document.getElementById("weekRangeLabel");
const healthStatus = document.getElementById("healthStatus");
const employeeBadge = document.getElementById("employeeBadge");

let allDesks = [];
let selectedDeskId = null;

function formatLocalDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}

const todayDate = new Date();
todayDate.setHours(0, 0, 0, 0);
const maxBookable = new Date(todayDate);
maxBookable.setDate(maxBookable.getDate() + 6);

const today = formatLocalDate(todayDate);
const maxBookableDate = formatLocalDate(maxBookable);

dateInput.min = today;
dateInput.max = maxBookableDate;
bookingDateInput.min = today;
bookingDateInput.max = maxBookableDate;

dateInput.value = today;
bookingDateInput.value = today;
employeeNameInput.value = employeeName;
employeeBadge.textContent = employeeName;
document.title = `DeskFlow — ${employeeName}`;

function isDateWithinBookingWindow(dateValue) {
    return Boolean(dateValue) &&
        dateValue >= today &&
        dateValue <= maxBookableDate;
}

function updateActiveDesksCount(availableCount, forDate) {
    document.getElementById("activeDesks").textContent = availableCount;
    const hint = document.getElementById("activeDesksHint");
    if (hint) {
        hint.textContent = forDate
            ? `Free on ${forDate}`
            : "Free on selected date";
    }
}

async function checkHealth() {
    try {
        const response = await fetch("/api/health");
        if (!response.ok) {
            throw new Error("API unavailable");
        }
        const result = await response.json();
        healthStatus.textContent =
            result.status === "ok" ? "API Online" : "API Status Unknown";
        healthStatus.className = "health-status online";
    } catch (error) {
        healthStatus.textContent = "API Offline";
        healthStatus.className = "health-status offline";
    }
}

async function loadDeskDatabase() {
    try {
        const response = await fetch("/api/desks");
        if (!response.ok) {
            throw new Error("Unable to load desk database");
        }
        allDesks = await response.json();
        updateOfficeStatistics();
        updateFloorOptions();
        await updateFloorOccupancy(dateInput.value);
    } catch (error) {
        console.error(error);
        showMessage("Unable to load office information.", true);
    }
}

function updateOfficeStatistics() {
    document.getElementById("totalDesks").textContent = allDesks.length;
}

async function updateFloorOccupancy(date) {
    const container = document.getElementById("floorStats");
    if (!container) {
        return;
    }

    const floors = [...new Set(allDesks.map(desk => desk.floor))]
        .sort((a, b) => a - b)
        .slice(0, 3);

    let bookings = [];
    if (date && isDateWithinBookingWindow(date)) {
        try {
            const response = await fetch(
                `/api/bookings?date=${encodeURIComponent(date)}`
            );
            if (response.ok) {
                bookings = await response.json();
            }
        } catch (error) {
            console.error(error);
        }
    }

    const bookedDeskIds = new Set(bookings.map(booking => booking.deskId));

    container.innerHTML = "";

    if (floors.length === 0) {
        container.innerHTML = `
            <div class="stat-card floor-stat-card">
                <span class="stat-label">Floors</span>
                <span class="floor-ratio">—</span>
            </div>
        `;
        return;
    }

    floors.forEach(floor => {
        const desksOnFloor = allDesks.filter(desk => desk.floor === floor);
        const total = desksOnFloor.length;
        const booked = desksOnFloor.filter(desk => bookedDeskIds.has(desk.id)).length;

        const card = document.createElement("div");
        card.className = "stat-card floor-stat-card";
        card.innerHTML = `
            <span class="stat-label">Floor ${floor}</span>
            <span class="floor-ratio">${booked}/${total}</span>
            <span class="stat-hint">${booked} booked · ${total} desks</span>
        `;
        container.appendChild(card);
    });
}

function updateFloorOptions() {
    floorSelect.innerHTML = '<option value="">All floors</option>';
    const floors = [...new Set(allDesks.map(desk => desk.floor))]
        .sort((a, b) => a - b);

    floors.forEach(floor => {
        const option = document.createElement("option");
        option.value = floor;
        option.textContent = `Floor ${floor}`;
        floorSelect.appendChild(option);
    });
}

async function loadAvailableDesks() {
    const date = dateInput.value;
    const floor = floorSelect.value;
    const monitor = monitorSelect.value;

    clearSelectedDesk();

    if (!date) {
        showMessage("Please select a date.", true);
        updateActiveDesksCount(0, null);
        return;
    }

    if (!isDateWithinBookingWindow(date)) {
        showMessage(
            `You can only search and book from ${today} to ${maxBookableDate} (7 days).`,
            true
        );
        updateActiveDesksCount(0, date);
        deskList.innerHTML = `
            <div class="empty-state">
                Choose a date within the next 7 days.
            </div>
        `;
        return;
    }

    // Unfiltered availability drives the Active Desks counter.
    try {
        const countResponse = await fetch(
            `/api/desks/available?date=${encodeURIComponent(date)}`
        );
        if (countResponse.ok) {
            const allAvailable = await countResponse.json();
            updateActiveDesksCount(allAvailable.length, date);
            await updateFloorOccupancy(date);
        }
    } catch (error) {
        console.error(error);
    }

    const query = new URLSearchParams();
    query.append("date", date);
    if (floor) {
        query.append("floor", floor);
    }
    if (monitor) {
        query.append("hasMonitor", monitor);
    }

    deskList.innerHTML = "";
    showMessage("Loading available desks...", false);

    try {
        const response = await fetch(`/api/desks/available?${query.toString()}`);
        if (!response.ok) {
            throw new Error("Unable to load available desks");
        }

        const desks = await response.json();

        if (desks.length === 0) {
            showMessage("No available desks found for these filters.", true);
            deskList.innerHTML = `
                <div class="empty-state">
                    Try another date, floor or monitor option.
                </div>
            `;
            return;
        }

        showMessage(`${desks.length} available desk(s) found.`, false);
        desks.forEach(renderDeskCard);
    } catch (error) {
        showMessage(error.message, true);
    }
}

function renderDeskCard(desk) {
    const card = document.createElement("div");
    card.className = "desk-card";
    card.id = `desk-card-${desk.id}`;

    card.innerHTML = `
        <h3>${escapeHtml(desk.code)}</h3>
        <p><strong>Desk ID:</strong> ${desk.id}</p>
        <p><strong>Floor:</strong> ${desk.floor}</p>
        <p><strong>Monitor:</strong> ${desk.hasMonitor ? "Yes" : "No"}</p>
        <span class="badge">Available</span>
        <button
            type="button"
            class="select-button"
            onclick="selectDesk(${desk.id}, '${escapeForJavaScript(desk.code)}')"
        >
            Select Desk
        </button>
    `;

    deskList.appendChild(card);
}

function selectDesk(deskId, deskCode) {
    selectedDeskId = deskId;
    deskIdInput.value = deskId;
    selectedDeskCodeInput.value = deskCode;
    bookingDateInput.value = dateInput.value;

    document.querySelectorAll(".desk-card").forEach(card => {
        card.classList.remove("selected");
    });

    const selectedCard = document.getElementById(`desk-card-${deskId}`);
    if (selectedCard) {
        selectedCard.classList.add("selected");
    }

    document.getElementById("createBookingSection").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

function clearSelectedDesk() {
    selectedDeskId = null;
    deskIdInput.value = "";
    selectedDeskCodeInput.value = "";
    document.querySelectorAll(".desk-card").forEach(card => {
        card.classList.remove("selected");
    });
}

bookingForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const deskId = Number(deskIdInput.value);
    const bookingDate = bookingDateInput.value;

    if (!deskId) {
        showBookingMessage("Please select an available desk first.", true);
        return;
    }

    if (!bookingDate) {
        showBookingMessage("Please select a booking date.", true);
        return;
    }

    if (!isDateWithinBookingWindow(bookingDate)) {
        showBookingMessage(
            `Bookings are only allowed from ${today} to ${maxBookableDate} (next 7 days).`,
            true
        );
        return;
    }

    const bookingRequest = {
        deskId: deskId,
        employeeName: employeeName,
        date: bookingDate
    };

    showBookingMessage("Creating booking...", false);

    try {
        const response = await fetch("/api/bookings", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(bookingRequest)
        });

        const result = await readResponseBody(response);

        if (!response.ok) {
            throw new Error(getErrorMessage(result, response.status));
        }

        showBookingMessage(
            `Booking created successfully. Booking ID: ${result.id}, Desk: ${result.deskCode}.`,
            false
        );

        dateInput.value = bookingDate;
        clearSelectedDesk();

        await loadAvailableDesks();
        await loadBookingsForDate();
        await loadMyWeekBookings();
    } catch (error) {
        showBookingMessage(error.message, true);
    }
});

async function loadBookingsForDate() {
    const date = dateInput.value;
    bookingList.innerHTML = "";

    if (!date) {
        showBookingListMessage("Please select a date.", true);
        return;
    }

    showBookingListMessage("Loading bookings...", false);

    try {
        const response = await fetch(
            `/api/bookings?date=${encodeURIComponent(date)}`
        );

        if (!response.ok) {
            throw new Error("Unable to load bookings");
        }

        const bookings = await response.json();

        if (bookings.length === 0) {
            showBookingListMessage("No bookings exist for this date.", false);
            bookingList.innerHTML = `
                <div class="empty-state">
                    All active desks are currently unbooked.
                </div>
            `;
            return;
        }

        showBookingListMessage(`${bookings.length} booking(s) for ${date}.`, false);
        bookings.forEach(booking => renderBookingItem(booking, bookingList, false));
    } catch (error) {
        showBookingListMessage(error.message, true);
    }
}

async function loadMyWeekBookings() {
    weekBookingList.innerHTML = "";
    showWeekMessage("Loading your week...", false);

    try {
        const response = await fetch(
            `/api/bookings/week?employeeName=${encodeURIComponent(employeeName)}`
        );

        if (!response.ok) {
            const result = await readResponseBody(response);
            throw new Error(getErrorMessage(result, response.status));
        }

        const payload = await response.json();
        weekRangeLabel.textContent =
            `Week ${payload.weekStart} → ${payload.weekEnd} (Mon–Sun)`;

        const bookings = payload.bookings || [];

        if (bookings.length === 0) {
            showWeekMessage("You have no bookings this week.", false);
            weekBookingList.innerHTML = `
                <div class="empty-state">
                    Book a desk below to see it here.
                </div>
            `;
            return;
        }

        showWeekMessage(`${bookings.length} booking(s) this week.`, false);
        bookings.forEach(booking => renderBookingItem(booking, weekBookingList, false));
    } catch (error) {
        showWeekMessage(error.message, true);
    }
}

function isOwnBooking(booking) {
    return booking.employeeName.trim().toLowerCase() ===
        employeeName.toLowerCase();
}

function renderBookingItem(booking, container, allowCancel) {
    const item = document.createElement("div");
    item.className = "booking-item";

    const own = isOwnBooking(booking);
    const canCancel = allowCancel && own;

    const actionHtml = canCancel
        ? `<button
                type="button"
                class="danger-button"
                onclick="cancelBooking(${booking.id})"
           >Cancel</button>`
        : allowCancel
            ? `<span class="locked-label">Others' booking</span>`
            : ``;

    item.innerHTML = `
        <div>
            <span class="booking-label">Desk</span>
            <h3>${escapeHtml(booking.deskCode)}</h3>
            <p>Desk ID: ${booking.deskId}</p>
        </div>
        <div>
            <span class="booking-label">Employee</span>
            <p><strong>${escapeHtml(booking.employeeName)}</strong></p>
        </div>
        <div>
            <span class="booking-label">Booking Information</span>
            <p>Date: ${booking.date}</p>
            <p>Booking ID: ${booking.id}</p>
        </div>
        <div>${actionHtml}</div>
    `;

    container.appendChild(item);
}

async function cancelBooking(bookingId) {
    const confirmed = window.confirm(`Cancel booking ${bookingId}?`);
    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `/api/bookings/${bookingId}?employeeName=${encodeURIComponent(employeeName)}`,
            { method: "DELETE" }
        );

        if (!response.ok) {
            const result = await readResponseBody(response);
            throw new Error(getErrorMessage(result, response.status));
        }

        showBookingListMessage(
            `Booking ${bookingId} cancelled successfully.`,
            false
        );

        await loadBookingsForDate();
        await loadAvailableDesks();
        await loadMyWeekBookings();
    } catch (error) {
        showBookingListMessage(error.message, true);
        showWeekMessage(error.message, true);
    }
}

async function readResponseBody(response) {
    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        return await response.json();
    }
    const text = await response.text();
    return text ? { message: text } : {};
}

function getErrorMessage(result, status) {
    return (
        result?.message ||
        result?.detail ||
        result?.error ||
        `Request failed with status ${status}`
    );
}

function showMessage(text, isError) {
    message.textContent = text;
    message.className = isError ? "message error" : "message success";
}

function showBookingMessage(text, isError) {
    bookingMessage.textContent = text;
    bookingMessage.className = isError ? "message error" : "message success";
}

function showBookingListMessage(text, isError) {
    bookingListMessage.textContent = text;
    bookingListMessage.className = isError ? "message error" : "message info";
}

function showWeekMessage(text, isError) {
    weekMessage.textContent = text;
    weekMessage.className = isError ? "message error" : "message info";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeForJavaScript(value) {
    return String(value)
        .replaceAll("\\", "\\\\")
        .replaceAll("'", "\\'");
}

document.getElementById("searchButton").addEventListener("click", async function () {
    await loadAvailableDesks();
    await loadBookingsForDate();
});

document.getElementById("refreshBookingsButton")
    .addEventListener("click", loadBookingsForDate);

document.getElementById("refreshWeekButton")
    .addEventListener("click", loadMyWeekBookings);

floorSelect.addEventListener("change", loadAvailableDesks);
monitorSelect.addEventListener("change", loadAvailableDesks);

dateInput.addEventListener("change", async function () {
    bookingDateInput.value = dateInput.value;
    await loadAvailableDesks();
    await loadBookingsForDate();
});

async function initialisePage() {
    await checkHealth();
    await loadDeskDatabase();
    await loadMyWeekBookings();
    await loadAvailableDesks();
    await loadBookingsForDate();
}

initialisePage();
