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
    fetch('http://localhost:8080/task/createTask', {
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
    .then(data => handleApiResponse(data, taskName, taskDescription, startTime, endTime)) // Pass taskName and taskDescription along with times
    .catch(error => {
        alert('Error connecting to the server: ' + error);
    });
}

// Handle the response from the API
function handleApiResponse(data, taskName, taskDescription, startTime, endTime) {
    if (data.message === 'Success') {
        createTaskElement(taskName, taskDescription, startTime, endTime);  // Pass taskName and taskDescription
    } else {
        alert('Failed to create task on the server.');
    }
}

// Create a visual element for the task in the scheduler
function createTaskElement(taskName, taskDescription, startTime, endTime) {
    const hourDivs = document.querySelectorAll('.hour');

    // Parse start and end time to get hours and minutes
    const [startHours, startMinutes] = startTime.split(':').map(Number);
    const [endHours, endMinutes] = endTime.split(':').map(Number);

    const startHourDiv = hourDivs[startHours];  // Find the div corresponding to the start hour

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

    // Add the task to the scheduler
    const scheduler = document.querySelector('.hours');
    scheduler.appendChild(task);
}

// Calculate task height for the visual scheduler
function calculateTaskHeight(startHours, startMinutes, endHours, endMinutes, hourHeight) {
    const totalMinutes = ((endHours - startHours) * 60) + (endMinutes - startMinutes);
    return (totalMinutes / 60) * hourHeight;
}

// Event listener for the Add Task button
document.getElementById('add-task-btn').addEventListener('click', addTask);
