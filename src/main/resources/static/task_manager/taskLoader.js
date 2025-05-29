let chosenDate;  // Define chosenDate in the global scope

// Get all hour divs
const hourDivs = document.querySelectorAll('.hour');
const modal = document.getElementById('task-modal');
const taskNameInput = document.getElementById('task-name');
const startTimeInput = document.getElementById('start-time');
const endTimeInput = document.getElementById('end-time');
const startDateInput = document.getElementById('start-date');
const endDateInput = document.getElementById('end-date');
const chosenDateInput = document.getElementById('chosen-date');

// Add event listeners for each hour div
hourDivs.forEach((hourDiv, index) => {
  hourDiv.setAttribute('data-hour', index % 24); // Assign hour based on index (0-23)
  hourDiv.setAttribute('data-day-offset', Math.floor(index / 24)); // Determine day offset

  hourDiv.addEventListener('click', () => {
    const hour = hourDiv.getAttribute('data-hour');
    const dayOffset = hourDiv.getAttribute('data-day-offset');

    // Calculate start and end times
    const startHour = hour.padStart(2, '0') + ':00';
    const endHour = (parseInt(hour) + 1).toString().padStart(2, '0') + ':00';

    // Get chosen date and calculate new date with day offset
    const chosenDate = new Date(chosenDateInput.value);
    chosenDate.setDate(chosenDate.getDate() + parseInt(dayOffset));
    const formattedDate = chosenDate.toISOString().split('T')[0];

    // Populate inputs
    startTimeInput.value = startHour;
    endTimeInput.value = endHour;
    startDateInput.value = formattedDate;
    endDateInput.value = formattedDate;
    taskNameInput.value = ''; // Clear task name for new input

    selectRandomColor();
    // Open modal
    modal.style.display = 'block';
  });
});

async function fetchWithAuth(url, options = {}) {
  let token = localStorage.getItem("jwtToken");

  let response = await fetch(url, {
    ...options,
    headers: {
      ...(options.headers || {}),
      "Authorization": `Bearer ${token}`,
    },
    credentials: "include"
  });

  if (response.status === 401 || response.status === 403) {
    // Try refresh
    const refreshResponse = await fetch("http://localhost:8080/auth/refresh-token", {
      method: "POST",
      credentials: "include"
    });

    if (!refreshResponse.ok) {
      console.warn("Refresh token expired or invalid");
      localStorage.removeItem("jwtToken");
      window.location.href = "./index.html";
      return;
    }

    const { accessToken } = await refreshResponse.json();
    localStorage.setItem("jwtToken", accessToken);

    console.log("Access token is: " + accessToken);

    // Retry original request
    response = await fetch(url, {
      ...options,
      headers: {
        ...(options.headers || {}),
        "Authorization": `Bearer ${accessToken}`,
      },
      credentials: "include"
    });
  }

  return response;
}

// Close modal functionality
document.getElementById('close-modal').addEventListener('click', () => {
    document.getElementById('task-name').value = '';
    document.getElementById('start-time').value = '';
    document.getElementById('end-time').value = '';
    document.getElementById('start-date').value = '';
    document.getElementById('end-date').value = '';
    document.getElementById('task-description').value = '';
    modal.style.display = 'none';
});

document.addEventListener('DOMContentLoaded', () => {
    const menuItems = document.querySelectorAll('.menu ul li');

    // Click handler for each <li>
    menuItems.forEach((item, index) => {
        item.addEventListener('click', () => {
            if (index === 0) {
                // Reload current page
                location.reload();
            } else if (index === 1) {
                // Navigate to profile.html
                window.location.href = './profile.html';
            }
        });
    });
});


document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('task-modal');
    const createTaskButton = document.getElementById('create-task-popup');
    const closeModal = document.getElementById('close-modal');

    createTaskButton.addEventListener('click', () => {
        selectRandomColor();
        modal.style.display = 'block';
    });

    closeModal.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    // Close the modal if the user clicks outside of it
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
});


// Get modal and button elements
//const modal = document.getElementById('task-modal');
/*const createTaskBtn = document.getElementById('create-task-popup');
const closeModalBtn = document.getElementById('close-modal');

// Open modal when the "Pridať" button is clicked
createTaskBtn.addEventListener('click', () => {
    modal.style.display = 'block';

});

// Close modal when the "x" button is clicked
closeModalBtn.addEventListener('click', () => {
    modal.style.display = 'none';
});*/

// Close modal if user clicks outside of the modal content
window.addEventListener('click', (event) => {
    if (event.target === modal) {
        modal.style.display = 'none';
    }
});

document.addEventListener("DOMContentLoaded", () => {
    const dateInput = document.getElementById('chosen-date');

    // When the date changes, update chosenDate and call onDateSet
    dateInput.addEventListener('input', (event) => {
        chosenDate = event.target.value; // Update chosenDate with the selected date
        onDateSet(); // Call the onDateSet function
    });
});

document.addEventListener("DOMContentLoaded", () => {
    const dateInput = document.getElementById('chosen-date');
    const prevDateBtn = document.getElementById('prev-date-btn');
    const nextDateBtn = document.getElementById('next-date-btn');

    // Function to change the date
    function changeDate(days) {
        const currentDate = new Date(chosenDate);
        currentDate.setDate(currentDate.getDate() + days);

        // Update chosenDate and the input field
        const day = String(currentDate.getDate()).padStart(2, '0');
        const month = String(currentDate.getMonth() + 1).padStart(2, '0');
        const year = currentDate.getFullYear();

        chosenDate = `${year}-${month}-${day}`;
        dateInput.value = chosenDate;

        // Call onDateSet to update the tasks and other elements
        onDateSet();
    }

    // Attach event listeners to the buttons
    prevDateBtn.addEventListener('click', () => changeDate(-1));
    nextDateBtn.addEventListener('click', () => changeDate(1));

    // Update chosenDate when the date input changes
    /*dateInput.addEventListener('input', (event) => {
        chosenDate = event.target.value;
       // onDateSet();
    });*/
});


async function onDateSet() {
    clearExistingTasks();

    const startDate = chosenDate;
    const endDate = getEndDate(startDate);  // Get the end date (4 days later)

    updateDayInfo(startDate);

    console.log("Pytam si tasky");
    const url = `http://localhost:8080/task/getTasks?startDate=${startDate}&endDate=${endDate}`;

    try {
        const response = await fetchWithAuth(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch tasks');
        }

        const data = await response.json();

        if (data.tasksList && Array.isArray(data.tasksList)) {
            data.tasksList.forEach(task => {
                createTaskElement(
                    task.taskName,
                    task.taskDescription,
                    task.startTime,
                    task.endTime,
                    task.startDate,
                    task.endDate,
                    task.id,
                    task.taskColor
                );
            });
        } else {
            console.warn("No tasks found in response.");
        }
    } catch (error) {
        console.error('Error calling getTasks API:', error.message);
        // Optionally redirect on auth error if fetchWithAuth didn't already handle it
    }
}

function updateDayInfo(startDate) {
    for (let i = 0; i <= 4; i++) {
        const currentDate = new Date(startDate); // Create a copy of startDate
        currentDate.setDate(currentDate.getDate() + i); // Add `i` days to the startDate

        // Extract day, month, and weekday
        const day = currentDate.getDate();
        const month = currentDate.toLocaleString('default', { month: 'long' });
        const weekday = currentDate.toLocaleString('default', { weekday: 'long' });

        // Find the target div
        const dayInfoDiv = document.getElementById(`day-info${i}`);
        if (dayInfoDiv) {
            // Update the content of the div
            dayInfoDiv.innerHTML = `
                <h4 class="day">${day}</h4>
                <h4 class="month">${month}</h4>
                <h4>${weekday}</h4>
            `;
        }
    }
}

// Function to clear all existing tasks in the scheduler divs
function clearExistingTasks() {
    // Select all divs with classes .hour, .hour1, .hour2, .hour3, .hour4
    const schedulerDivs = document.querySelectorAll('.hours, .hours1, .hours2, .hours3, .hours4');

    // Loop through each scheduler div
    schedulerDivs.forEach(div => {
        // Find all child elements with the class "task" inside this div
        const taskElements = div.querySelectorAll('.task');

        // Remove each task element
        taskElements.forEach(task => task.remove());
    });
    clearTaskList();
}

// Function to get the endDate (4 days later)
function getEndDate(startDate) {
    const start = new Date(startDate);  // Convert startDate string to Date object
    start.setDate(start.getDate() + 4);  // Add 4 days to the start date

    // Format the end date as YYYY-MM-DD
    const day = String(start.getDate()).padStart(2, '0');
    const month = String(start.getMonth() + 1).padStart(2, '0'); // Months are 0-based
    const year = start.getFullYear();

    return `${year}-${month}-${day}`;
}

// Function to set the chosenDate to today's date and then call onDateSet
function setChosenDate() {
    const today = new Date();
    const day = String(today.getDate()).padStart(2, '0');
    const month = String(today.getMonth() + 1).padStart(2, '0'); // Months are 0-based
    const year = today.getFullYear();

    //hideHoursBefore(10);

    // Format chosenDate as YYYY-MM-DD
    chosenDate = `${year}-${month}-${day}`;

    // Set the value of the input field with id "chosen-date" to the current date
    const dateInput = document.getElementById('chosen-date');
    dateInput.value = chosenDate;

    // Set chosenDate globally so that it can be used in onDateSet
    window.chosenDate = chosenDate;

    // Call onDateSet if needed
    onDateSet();
}

// Calculate task height for the visual scheduler
function calculateTaskHeight(startHours, startMinutes, endHours, endMinutes, hourHeight) {
    const totalMinutes = ((endHours - startHours) * 60) + (endMinutes - startMinutes);
    return (totalMinutes / 60) * hourHeight;
}

function hideHoursBefore(index) {
    // Select all hour divs
    const hourDivs = document.querySelectorAll('.hours .hour');

    // Loop through the hour divs
    hourDivs.forEach((hourDiv, i) => {
        if (i < index) {
            // Hide the .hour div
            hourDiv.style.display = 'none';

            // Hide the next sibling <hr> after the .hour div
            const nextHr = hourDiv.nextElementSibling;
            if (nextHr && nextHr.tagName === 'HR') {
                nextHr.style.display = 'none';
            }
        } else {
            // Make sure the remaining elements are visible
            hourDiv.style.display = '';

            const nextHr = hourDiv.nextElementSibling;
            if (nextHr && nextHr.tagName === 'HR') {
                nextHr.style.display = '';
            }
        }
    });
}

// Run the setChosenDate function when the page loads
window.addEventListener('load', setChosenDate);
