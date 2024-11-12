// Function to handle adding a new task
function addTask() {
    const taskName = document.getElementById('task-name').value;
    const startTime = document.getElementById('start-time').value; // Format: "HH:MM"
    const endTime = document.getElementById('end-time').value;     // Format: "HH:MM"
    const startDate = document.getElementById('start-date').value;
    const endDate = document.getElementById('end-date').value;
    const taskDescription = document.getElementById('task-description').value;

    if (!startTime || !endTime || !startDate || !endDate || !taskName) {
        alert('Please fill out all required fields.');
        return;
    }

    // Create a task object
    const task = {
        taskName,
        startTime,
        endTime,
        startDate,
        endDate,
        taskDescription
    };

    // Make the POST request with a JSON body
    fetch('http://192.168.1.36:8080/task/createTask', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(task)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to fetch');
        }
        return response.json();
    })
    .then(data => handleApiResponse(data, taskName, taskDescription, startTime, endTime, startDate, endDate)) // Pass taskName and taskDescription along with times
    .catch(error => {
        alert('Error connecting to the server:  ' + error);
    });
}

// Handle the response from the API
function handleApiResponse(data, taskName, taskDescription, startTime, endTime, startDate, endDate) {
    if (data.message === 'Success') {
        createTaskElement(taskName, taskDescription, startTime, endTime, startDate, endDate);  // Pass taskName and taskDescription
    } else {
        alert('Failed to create task on the server.');
    }
}

// Create a visual element for the task in the scheduler
function createTaskElement(taskName, taskDescription, startTime, endTime, startDate, endDate) {
    const hourDivs = document.querySelectorAll('.hour');

    // Parse start and end time to get hours and minutes
    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const [endHours, endMinutes] = endTime.split(':').map(Number);

    // Convert startDate to a Date object for comparison
    const taskStartDate = new Date(startDate); // assuming startDate is in 'YYYY-MM-DD' format
    const chosenDateObj = new Date(chosenDate); // chosenDate is also assumed to be a Date object

    // Calculate the difference in days between chosenDate and taskStartDate
    const dayDifference = Math.floor((taskStartDate - chosenDateObj) / (1000 * 60 * 60 * 24));

    // Check if dayDifference is within 0 to 4
    if (dayDifference < 0 || dayDifference > 4) {
        alert('Task date is out of the allowed range.');
        return;
    }

    // Select the correct scheduler div based on dayDifference
    const scheduler = document.querySelector(`#hours${dayDifference === 0 ? '' : dayDifference}`);

    if (!scheduler) {
        alert('Could not find the corresponding scheduler div. ' + dayDifference);
        return;
    }

    const startHourDiv = hourDivs[startHours];
    if (!startHourDiv) {
        alert('Could not find the corresponding hour div.');
        return;
    }

    // Create a new task element
    const task = document.createElement('div');
    task.className = 'task';

    // Task title (taskName)
    const taskTitle = document.createElement('h3');
    taskTitle.className = 'task-title';
    taskTitle.textContent = taskName;

    // Task description
    const taskDesc = document.createElement('p');
    taskDesc.className = 'task-desc';
    taskDesc.textContent = taskDescription;

    // Append the title and description to the task div
    task.appendChild(taskTitle);
    task.appendChild(taskDesc);

    // Calculate position and height based on start and end time
    const hourHeight = hourDivs[0].offsetHeight;
    const startMinuteOffset = (startMinutes / 60) * hourHeight;
    const taskTop = startHourDiv.offsetTop + startMinuteOffset;
    const taskHeight = calculateTaskHeight(startHours, startMinutes, endHours, endMinutes, hourHeight);

    // Position and size the task
    task.style.top = `${taskTop}px`;
    task.style.height = `${taskHeight}px`;

    // Add the task to the appropriate scheduler div
    scheduler.appendChild(task);
}

// Calculate task height for the visual scheduler
function calculateTaskHeight(startHours, startMinutes, endHours, endMinutes, hourHeight) {
    const totalMinutes = ((endHours - startHours) * 60) + (endMinutes - startMinutes);
    return (totalMinutes / 60) * hourHeight;
}

// Event listener for the Add Task button
document.getElementById('add-task-btn').addEventListener('click', addTask);
