const deskList = document.getElementById("deskList");
const message = document.getElementById("message");
const bookingMessage = document.getElementById("bookingMessage");

document.getElementById("date").value =
    new Date().toISOString().split("T")[0];

document.getElementById("bookingDate").value =
    new Date().toISOString().split("T")[0];

async function loadAvailableDesks() {
    const date = document.getElementById("date").value;
    const floor = document.getElementById("floor").value;
    const monitor = document.getElementById("monitor").value;

    if (!date) {
        showMessage("Please select a date.", true);
        return;
    }

    let url = `/api/desks/available?date=${date}`;

    if (floor) {
        url += `&floor=${floor}`;
    }

    if (monitor) {
        url += `&hasMonitor=${monitor}`;
    }

    deskList.innerHTML = "";
    showMessage("Loading...", false);

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error("Unable to load desks");
        }

        const desks = await response.json();

        if (desks.length === 0) {
            showMessage("No available desks found.", true);
            return;
        }

        showMessage(`${desks.length} available desk(s) found.`, false);

        desks.forEach(desk => {
            const card = document.createElement("div");
            card.className = "desk-card";

            card.innerHTML = `
                <h3>${desk.code}</h3>
                <p><strong>Desk ID:</strong> ${desk.id}</p>
                <p><strong>Floor:</strong> ${desk.floor}</p>
                <p><strong>Monitor:</strong> ${desk.hasMonitor ? "Yes" : "No"}</p>
                <button onclick="selectDesk(${desk.id})">
                    Select Desk
                </button>
            `;

            deskList.appendChild(card);
        });

    } catch (error) {
        showMessage(error.message, true);
    }
}

function selectDesk(deskId) {
    document.getElementById("deskId").value = deskId;

    const selectedDate = document.getElementById("date").value;
    document.getElementById("bookingDate").value = selectedDate;

    window.scrollTo({
        top: document.body.scrollHeight,
        behavior: "smooth"
    });
}

document
    .getElementById("bookingForm")
    .addEventListener("submit", async function(event) {
        event.preventDefault();

        const booking = {
            deskId: Number(document.getElementById("deskId").value),
            employeeName: document
                .getElementById("employeeName")
                .value
                .trim(),
            date: document.getElementById("bookingDate").value
        };

        bookingMessage.textContent = "Creating booking...";
        bookingMessage.className = "";

        try {
            const response = await fetch("/api/bookings", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(booking)
            });

            let result = null;

            try {
                result = await response.json();
            } catch {
                result = null;
            }

            if (!response.ok) {
                const errorText =
                    result?.message ||
                    result?.error ||
                    "Booking failed";

                throw new Error(errorText);
            }

            bookingMessage.textContent =
                `Booking created successfully. Booking ID: ${result.id}`;

            bookingMessage.className = "success";

            loadAvailableDesks();

        } catch (error) {
            bookingMessage.textContent = error.message;
            bookingMessage.className = "error";
        }
    });

function showMessage(text, isError) {
    message.textContent = text;
    message.className = isError ? "error" : "success";
}
