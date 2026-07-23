const dateInput = document.getElementById("date");
const floorSelect = document.getElementById("floor");
const monitorSelect = document.getElementById("monitor");

const deskList = document.getElementById("deskList");
const message = document.getElementById("message");

const bookingForm = document.getElementById("bookingForm");
const deskIdInput = document.getElementById("deskId");
const selectedDeskCodeInput =
    document.getElementById("selectedDeskCode");

const employeeNameInput =
    document.getElementById("employeeName");

const bookingDateInput =
    document.getElementById("bookingDate");

const bookingMessage =
    document.getElementById("bookingMessage");

const bookingList =
    document.getElementById("bookingList");

const bookingListMessage =
    document.getElementById("bookingListMessage");

const healthStatus =
    document.getElementById("healthStatus");

let allDesks = [];
let selectedDeskId = null;

const today = new Date().toISOString().split("T")[0];

dateInput.value = today;
bookingDateInput.value = today;

/*
 * Check Spring Boot health endpoint.
 */
async function checkHealth() {
    try {
        const response = await fetch("/api/health");

        if (!response.ok) {
            throw new Error("API unavailable");
        }

        const result = await response.json();

        healthStatus.textContent =
            result.status === "ok"
                ? "API Online"
                : "API Status Unknown";

        healthStatus.className =
            "health-status online";

    } catch (error) {
        healthStatus.textContent = "API Offline";
        healthStatus.className =
            "health-status offline";
    }
}

/*
 * Load every desk from the database.
 *
 * This is used for:
 * - office statistics;
 * - dynamic floor dropdown;
 * - matching desk IDs with desk codes.
 */
async function loadDeskDatabase() {
    try {
        const response = await fetch("/api/desks");

        if (!response.ok) {
            throw new Error("Unable to load desk database");
        }

        allDesks = await response.json();

        updateOfficeStatistics();
        updateFloorOptions();

    } catch (error) {
        console.error(error);
        showMessage(
            "Unable to load office information.",
            true
        );
    }
}

/*
 * Calculate statistics from all desks.
 */
function updateOfficeStatistics() {
    const totalDesks = allDesks.length;

    const activeDesks = allDesks.filter(
        desk => desk.active
    ).length;

    const monitorDesks = allDesks.filter(
        desk => desk.hasMonitor
    ).length;

    const floors = [
        ...new Set(
            allDesks.map(desk => desk.floor)
        )
    ];

    document.getElementById("totalDesks")
        .textContent = totalDesks;

    document.getElementById("activeDesks")
        .textContent = activeDesks;

    document.getElementById("monitorDesks")
        .textContent = monitorDesks;

    document.getElementById("floorCount")
        .textContent = floors.length;
}

/*
 * Create floor options from database values.
 */
function updateFloorOptions() {
    floorSelect.innerHTML =
        '<option value="">All floors</option>';

    const floors = [
        ...new Set(
            allDesks.map(desk => desk.floor)
        )
    ].sort((a, b) => a - b);

    floors.forEach(floor => {
        const option = document.createElement("option");

        option.value = floor;
        option.textContent = `Floor ${floor}`;

        floorSelect.appendChild(option);
    });
}

/*
 * Load active and available desks.
 */
async function loadAvailableDesks() {
    const date = dateInput.value;
    const floor = floorSelect.value;
    const monitor = monitorSelect.value;

    clearSelectedDesk();

    if (!date) {
        showMessage("Please select a date.", true);
        return;
    }

    const params = new URLSearchParams();

    params.append("date", date);

    if (floor) {
        params.append("floor", floor);
    }

    if (monitor) {
        params.append("hasMonitor", monitor);
    }

    deskList.innerHTML = "";

    showMessage("Loading available desks...", false);

    try {
        const response = await fetch(
            `/api/desks/available?${params.toString()}`
        );

        if (!response.ok) {
            throw new Error(
                "Unable to load available desks"
            );
        }

        const desks = await response.json();

        if (desks.length === 0) {
            showMessage(
                "No available desks found for these filters.",
                true
            );

            deskList.innerHTML = `
                <div class="empty-state">
                    Try another date, floor or monitor option.
                </div>
            `;

            return;
        }

        showMessage(
            `${desks.length} available desk(s) found.`,
            false
        );

        desks.forEach(renderDeskCard);

    } catch (error) {
        showMessage(error.message, true);
    }
}

/*
 * Create one available desk card.
 */
function renderDeskCard(desk) {
    const card = document.createElement("div");

    card.className = "desk-card";
    card.id = `desk-card-${desk.id}`;

    card.innerHTML = `
        <h3>${escapeHtml(desk.code)}</h3>

        <p>
            <strong>Desk ID:</strong>
            ${desk.id}
        </p>

        <p>
            <strong>Floor:</strong>
            ${desk.floor}
        </p>

        <p>
            <strong>Monitor:</strong>
            ${desk.hasMonitor ? "Yes" : "No"}
        </p>

        <span class="badge">Available</span>

        <button
            type="button"
            class="select-button"
            onclick="selectDesk(
                ${desk.id},
                '${escapeForJavaScript(desk.code)}'
            )"
        >
            Select Desk
        </button>
    `;

    deskList.appendChild(card);
}

/*
 * Select an available desk for booking.
 */
function selectDesk(deskId, deskCode) {
    selectedDeskId = deskId;

    deskIdInput.value = deskId;
    selectedDeskCodeInput.value = deskCode;
    bookingDateInput.value = dateInput.value;

    document
        .querySelectorAll(".desk-card")
        .forEach(card => {
            card.classList.remove("selected");
        });

    const selectedCard =
        document.getElementById(`desk-card-${deskId}`);

    if (selectedCard) {
        selectedCard.classList.add("selected");
    }

    document
        .getElementById("createBookingSection")
        .scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
}

/*
 * Clear selected desk.
 */
function clearSelectedDesk() {
    selectedDeskId = null;

    deskIdInput.value = "";
    selectedDeskCodeInput.value = "";

    document
        .querySelectorAll(".desk-card")
        .forEach(card => {
            card.classList.remove("selected");
        });
}

/*
 * Create booking.
 */
bookingForm.addEventListener(
    "submit",
    async function(event) {
        event.preventDefault();

        const deskId = Number(deskIdInput.value);

        const employeeName =
            employeeNameInput.value.trim();

        const bookingDate =
            bookingDateInput.value;

        if (!deskId) {
            showBookingMessage(
                "Please select an available desk first.",
                true
            );

            return;
        }

        if (!employeeName) {
            showBookingMessage(
                "Employee name cannot be blank.",
                true
            );

            return;
        }

        if (!bookingDate) {
            showBookingMessage(
                "Please select a booking date.",
                true
            );

            return;
        }

        const bookingRequest = {
            deskId: deskId,
            employeeName: employeeName,
            date: bookingDate
        };

        showBookingMessage(
            "Creating booking...",
            false
        );

        try {
            const response = await fetch(
                "/api/bookings",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify(
                        bookingRequest
                    )
                }
            );

            const result =
                await readResponseBody(response);

            if (!response.ok) {
                throw new Error(
                    getErrorMessage(
                        result,
                        response.status
                    )
                );
            }

            showBookingMessage(
                `Booking created successfully. ` +
                `Booking ID: ${result.id}, ` +
                `Desk: ${result.deskCode}.`,
                false
            );

            employeeNameInput.value = "";

            dateInput.value = bookingDate;

            clearSelectedDesk();

            await loadAvailableDesks();
            await loadBookingsForDate();

        } catch (error) {
            showBookingMessage(
                error.message,
                true
            );
        }
    }
);

/*
 * Load all bookings for the selected date.
 */
async function loadBookingsForDate() {
    const date = dateInput.value;

    bookingList.innerHTML = "";

    if (!date) {
        showBookingListMessage(
            "Please select a date.",
            true
        );

        return;
    }

    showBookingListMessage(
        "Loading bookings...",
        false
    );

    try {
        const response = await fetch(
            `/api/bookings?date=${encodeURIComponent(date)}`
        );

        if (!response.ok) {
            throw new Error(
                "Unable to load bookings"
            );
        }

        const bookings = await response.json();

        if (bookings.length === 0) {
            showBookingListMessage(
                "No bookings exist for this date.",
                false
            );

            bookingList.innerHTML = `
                <div class="empty-state">
                    All active desks are currently unbooked.
                </div>
            `;

            return;
        }

        showBookingListMessage(
            `${bookings.length} booking(s) for ${date}.`,
            false
        );

        bookings.forEach(renderBookingItem);

    } catch (error) {
        showBookingListMessage(
            error.message,
            true
        );
    }
}

/*
 * Render one existing booking.
 */
function renderBookingItem(booking) {
    const item = document.createElement("div");

    item.className = "booking-item";

    item.innerHTML = `
        <div>
            <span class="booking-label">
                Desk
            </span>

            <h3>
                ${escapeHtml(booking.deskCode)}
            </h3>

            <p>Desk ID: ${booking.deskId}</p>
        </div>

        <div>
            <span class="booking-label">
                Employee
            </span>

            <p>
                <strong>
                    ${escapeHtml(booking.employeeName)}
                </strong>
            </p>
        </div>

        <div>
            <span class="booking-label">
                Booking Information
            </span>

            <p>Date: ${booking.date}</p>
            <p>Booking ID: ${booking.id}</p>
        </div>

        <div>
            <button
                type="button"
                class="danger-button"
                onclick="cancelBooking(${booking.id})"
            >
                Cancel
            </button>
        </div>
    `;

    bookingList.appendChild(item);
}

/*
 * Cancel an existing booking.
 */
async function cancelBooking(bookingId) {
    const confirmed = window.confirm(
        `Cancel booking ${bookingId}?`
    );

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(
            `/api/bookings/${bookingId}`,
            {
                method: "DELETE"
            }
        );

        if (!response.ok) {
            const result =
                await readResponseBody(response);

            throw new Error(
                getErrorMessage(
                    result,
                    response.status
                )
            );
        }

        showBookingListMessage(
            `Booking ${bookingId} cancelled successfully.`,
            false
        );

        await loadBookingsForDate();
        await loadAvailableDesks();

    } catch (error) {
        showBookingListMessage(
            error.message,
            true
        );
    }
}

/*
 * Safely read JSON or text response.
 */
async function readResponseBody(response) {
    const contentType =
        response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
        return await response.json();
    }

    const text = await response.text();

    return text
        ? { message: text }
        : {};
}

/*
 * Extract useful error text from Spring Boot response.
 */
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

    message.className =
        isError
            ? "message error"
            : "message success";
}

function showBookingMessage(text, isError) {
    bookingMessage.textContent = text;

    bookingMessage.className =
        isError
            ? "message error"
            : "message success";
}

function showBookingListMessage(text, isError) {
    bookingListMessage.textContent = text;

    bookingListMessage.className =
        isError
            ? "message error"
            : "message info";
}

/*
 * Prevent inserted database text from becoming HTML.
 */
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

/*
 * Automatically update results when filters change.
 */
document
    .getElementById("searchButton")
    .addEventListener(
        "click",
        async function() {
            await loadAvailableDesks();
            await loadBookingsForDate();
        }
    );

document
    .getElementById("refreshBookingsButton")
    .addEventListener(
        "click",
        loadBookingsForDate
    );

floorSelect.addEventListener(
    "change",
    loadAvailableDesks
);

monitorSelect.addEventListener(
    "change",
    loadAvailableDesks
);

dateInput.addEventListener(
    "change",
    async function() {
        bookingDateInput.value =
            dateInput.value;

        await loadAvailableDesks();
        await loadBookingsForDate();
    }
);

/*
 * Initial page loading.
 */
async function initialisePage() {
    await checkHealth();
    await loadDeskDatabase();
    await loadAvailableDesks();
    await loadBookingsForDate();
}

initialisePage();
