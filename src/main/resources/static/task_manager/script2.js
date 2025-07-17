
let currentId;
let currentTask;
let taskList = [];
let currentStartTime;
let currentEndTime;
let currentTaskOffset;
let currentTaskIndex;
let currentTaskDayDiff;
let lastUsedColor;
let selectedTaskColor = null;
let selectedTaskColorUpdate = null;
const successPopup = document.getElementById('success-popup');

const BASE_API_URL = 'https://ba12-46-151-56-119.ngrok-free.app';

document.querySelectorAll('.color-btn-update').forEach(button => {
    button.addEventListener('click', () => {
        selectedTaskColorUpdate = button.getAttribute('data-color');
        console.log('Selected color update:', selectedTaskColorUpdate);

        document.querySelectorAll('.color-btn-update').forEach(btn => btn.classList.remove('selected'));
        button.classList.add('selected');
    });
});

document.querySelectorAll('.color-btn').forEach(button => {
    button.addEventListener('click', () => {
        selectedTaskColor = button.getAttribute('data-color');
        console.log('Selected color:', selectedTaskColor);

        document.querySelectorAll('.color-btn').forEach(btn => btn.classList.remove('selected'));
        button.classList.add('selected');
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
    const refreshResponse = await fetch(`${BASE_API_URL}/auth/refresh-token`, {
      method: "POST",
      credentials: "include"
    });

    if (!refreshResponse.ok) {
      console.warn("Refresh token expired or invalid");
      localStorage.removeItem("jwtToken");
      window.location.href = "../index.html";
      return;
    }

    const { token } = await refreshResponse.json();
    localStorage.setItem("jwtToken", token);

    console.log("Refresh token response: " + token);

    // Retry original request
    response = await fetch(url, {
      ...options,
      headers: {
        ...(options.headers || {}),
        "Authorization": `Bearer ${token}`,
      },
      credentials: "include"
    });
  }

  return response;
}


function selectRandomColor() {
    const colorButtons = document.querySelectorAll('.color-btn');

    if (colorButtons.length === 0) return;

    // Randomly select a button
    const randomIndex = Math.floor(Math.random() * colorButtons.length);
    const randomButton = colorButtons[randomIndex];

    // Update the selectedTaskColor to the randomly selected button's color
    selectedTaskColor = randomButton.getAttribute('data-color');
    console.log('Randomly selected color:', selectedTaskColor);

    // Add the visual indicator to the selected button
    colorButtons.forEach(button => button.classList.remove('selected'));
    randomButton.classList.add('selected');
}


async function addTask() {
    const taskName = document.getElementById('task-name').value;
    const startTime = document.getElementById('start-time').value;
    const endTime = document.getElementById('end-time').value;
    const startDate = document.getElementById('start-date').value;
    const endDate = document.getElementById('end-date').value;
    const taskDescription = document.getElementById('task-description').value;
    const colors = ['#ff2c2c', '#007bff', '#4CAF50', '#8F00FF', '#FF6C3A', '#232C3B', '#ff007f', '#FFAA1D', '#2752D6'];
    let taskColor = selectedTaskColor;


    console.log("Chosen color for task is: " + taskColor);

    if (!startTime || !endTime || !startDate || !endDate || !taskName) {
        alert('Please fill out all required fields.');
        return;
    }

    closeTaskModalNormal();

    const task = {
        taskName,
        startTime,
        endTime,
        startDate,
        endDate,
        taskDescription,
        taskColor
    };

    try {
        const response = await fetchWithAuth(`${BASE_API_URL}/task/createTask`, { // Using BASE_API_URL
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(task)
        });

        if (!response.ok) {
            throw new Error('Failed to create task');
        }

        const data = await response.json();
        handleApiResponse(data, taskName, taskDescription, startTime, endTime, startDate, endDate);
    } catch (error) {
        alert('Error connecting to the server: ' + error.message);
    }
}



async function updateTask() {
    const taskName = document.getElementById('task-name-update').value;
    const startTime = document.getElementById('start-time-update').value;
    const endTime = document.getElementById('end-time-update').value;
    const startDate = document.getElementById('start-date-update').value;
    const endDate = document.getElementById('end-date-update').value;
    const taskDescription = document.getElementById('task-description-update').value;


    if (!startTime || !endTime || !startDate || !endDate || !taskName) {
        alert('Please fill out all required fields.');
        return;
    }

    closeTaskModal();

    let taskColor = selectedTaskColorUpdate;
    const id = currentId;

    const task = {
        id,
        taskName,
        startTime,
        endTime,
        startDate,
        endDate,
        taskDescription,
        taskColor
    };

    try {
        const response = await fetchWithAuth(`${BASE_API_URL}/task/updateTask`, { // Using BASE_API_URL
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(task)
        });

        if (!response.ok) {
            throw new Error('Failed to update task');
        }

        const data = await response.json();
        handleApiResponseUpdate(data, taskName, taskDescription, startTime, endTime, startDate, endDate);
    } catch (error) {
        alert('Error connecting to the server: ' + error.message);
    }
}


function handleApiResponseUpdate(data, taskName, taskDescription, startTime, endTime, startDate, endDate) {
    if (data.message === 'Success') {
        removeTask(currentTaskIndex, currentTaskDayDiff)
        createTaskElement(taskName, taskDescription, startTime, endTime, startDate, endDate, data.task.id, data.task.taskColor);  // Pass taskName and taskDescription
    } else {
        alert('Failed to update task on the server.');
    }
}

function removeTask(index, dayDifference) {
      for (let i = index; i <= index + dayDifference; i++) {
            console.log("Removing");
            taskList[i].remove();
      }
}

// Handle the response from the API
function handleApiResponse(data, taskName, taskDescription, startTime, endTime, startDate, endDate) {
    if (data.message === 'Success') {
        console.log("V handle api response task color je: " + data.taskColor);
        createTaskElement(taskName, taskDescription, startTime, endTime, startDate, endDate, data.task.id, data.task.taskColor);  // Pass taskName and taskDescription
    } else {
        alert('Failed to create task on the server.');
    }
}

function createTaskElement(taskName, taskDescription, startTime, endTime, startDate, endDate, id, taskColor) {
    const hourDivs = document.querySelectorAll('.hour');

    console.log("Create task element dates: " + startDate + " " + endDate);

    let [startHours, startMinutes] = startTime.split(':').map(Number);
    let [endHours, endMinutes] = endTime.split(':').map(Number);
    // Convert startDate to a Date object for comparison
    const taskStartDate = new Date(startDate); // assuming startDate is in 'YYYY-MM-DD' format
    const taskEndDate = new Date(endDate);
    const chosenDateObj = new Date(chosenDate); // chosenDate is also assumed to be a Date object

    currentStartTime = startTime;
    currentEndTime = endTime;

    // Calculate the difference in days between chosenDate and taskStartDate
    const dayDifference = Math.floor((taskStartDate - chosenDateObj) / (1000 * 60 * 60 * 24));

    if (dayDifference > 4) {
        alert('Task date is out of the allowed range.');
        return;
    }
    if (taskEndDate < chosenDate) {
         alert('Task endDate is before chosenDate');
         return;
    }

    if (taskStartDate.getTime() !== taskEndDate.getTime()) {
        if (taskStartDate < chosenDateObj) {
            startHours = 0;
            startMinutes = 0;
            taskStartDate.setFullYear(chosenDateObj.getFullYear());
            taskStartDate.setMonth(chosenDateObj.getMonth());
            taskStartDate.setDate(chosenDateObj.getDate());
        }
        const daysBetweenStartAndEnd = Math.floor((taskEndDate - taskStartDate) / (1000 * 60 * 60 * 24));
        let index =  Math.floor((taskStartDate - chosenDateObj) / (1000 * 60 * 60 * 24));
        console.log(`Days between startDate and endDate: ${daysBetweenStartAndEnd}`);
        console.log(`Index (days between chosenDate and startDate): ${index}`);

        let iterations = 5 - index;
        let lastDayEnd = false;

        if(daysBetweenStartAndEnd < iterations) {
            lastDayEnd = true;
            iterations= daysBetweenStartAndEnd;
        }
        for (let i = 0; i < iterations; i++) {
            const scheduler = document.querySelector(`#hours${index === 0 ? '' : index}`);
            if (!scheduler) {
                  console.log(`No scheduler found for index: ${index}`);
                  continue;
            }
            let startHoursStart = i === 0 ? startHours : 0; // Use startHours for the first day
            let startMinutesStart = i === 0 ? startMinutes : 0; // Use startMinutes for the first day
            let endHoursEnd = 23; // End at 24:00
            let endMinutesEnd = 59;
            const hideButton = i === 0 ? false : true;
            console.log("IsFirst: " + hideButton);
            createAndAppendTaskDiv(scheduler, taskName, taskDescription, startHoursStart, startMinutesStart, endHoursEnd, endMinutesEnd, hourDivs, id, startDate, endDate, hideButton, taskColor);
            index++;
        }

        if(lastDayEnd) {
            const scheduler = document.querySelector(`#hours${index === 0 ? '' : index}`);
            let startHoursStart = 0;
            let startMinutesStart = 0;
            createAndAppendTaskDiv(scheduler, taskName, taskDescription, startHoursStart, startMinutesStart, endHours, endMinutes, hourDivs, id, startDate, endDate, true, taskColor);
        }
        console.log("Im in startdate does not equal enddate");
    } else {
    // Parse start and end time to get hours and minutes

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
   console.log("Im in startdate is equal to enddate");
   createAndAppendTaskDiv(scheduler, taskName, taskDescription, startHours, startMinutes, endHours, endMinutes, hourDivs, id, startDate, endDate, false, taskColor);
   }
}

function createAndAppendTaskDiv(scheduler, taskName, taskDescription, startHours, startMinutes, endHours, endMinutes, hourDivs, id, startDate, endDate, hideDeleteButton, taskColor) {
    const task = document.createElement('div');
    task.className = 'task';

    const taskTitle = document.createElement('h3');
    taskTitle.className = 'task-title';
    taskTitle.textContent = taskName;

    // Parse the dates into Date objects
    const startDateTime = new Date(`${startDate}T${String(startHours).padStart(2, '0')}:${String(startMinutes).padStart(2, '0')}`);
    const endDateTime = new Date(`${endDate}T${String(endHours).padStart(2, '0')}:${String(endMinutes).padStart(2, '0')}`);

    const durationInMinutes = (endDateTime - startDateTime) / (1000 * 60); // Convert milliseconds to minutes

    const isMultiDay = startDate !== endDate;
    const taskDesc = document.createElement('p');
    if (durationInMinutes >= 35 || isMultiDay) {
         taskDesc.className = 'task-desc';
         taskDesc.textContent = taskDescription;
    }

    console.log("Hide delete button: " + hideDeleteButton);
    const currentTaskListLength = taskList.length;


   const taskStartDate = new Date(startDate);
   const taskEndDate = new Date(endDate);
   const chosenDateObj = new Date(chosenDate);
   const chosenDateEnd = new Date(chosenDate);
   chosenDateEnd.setDate(chosenDateEnd.getDate() + 4);

   const relativeTaskStart = taskStartDate < chosenDateObj ? new Date(chosenDateObj) : new Date(taskStartDate);
   const relativeTaskEnd = taskEndDate > chosenDateEnd ? new Date(chosenDateEnd) : new Date(taskEndDate);

   const dayDifference = Math.floor((relativeTaskEnd - relativeTaskStart) / (1000 * 60 * 60 * 24));

    // Close button
    if(hideDeleteButton != true) {
        currentTaskOffset = 0;
        const closeButton = document.createElement('span');
        closeButton.className = 'delete-btn-task';
        closeButton.innerHTML = '&times;';

        closeButton.onclick = (event) => {
            console.log("Length when event was registered: " + currentTaskListLength);
            event.stopPropagation(); // Prevent triggering the parent element's click event
            deleteTask(id, currentTaskListLength, dayDifference);
        };

        task.appendChild(closeButton);
    } else {
        currentTaskOffset++;
    }

    task.appendChild(taskTitle);
    task.appendChild(taskDesc);

    // Calculate position and height based on start and end time
    const hourHeight = hourDivs[0].offsetHeight;
    const startMinuteOffset = (startMinutes / 60) * hourHeight;
    const taskTop = hourDivs[startHours].offsetTop + startMinuteOffset;
    const taskHeight = calculateTaskHeight(taskTop, endHours, endMinutes, hourHeight, hourDivs);

    console.log("Assigning color to task element, color is: " + taskColor);
    task.style.backgroundColor = taskColor;
    task.style.position = 'absolute';
    task.style.top = `${taskTop}px`;
    task.style.height = `${taskHeight}px`;

    // Add click event to open modal with pre-filled task data
    console.log("In create and append taskDiv " + startDate + " " + endDate);
    const timeStart = currentStartTime;
    const timeEnd = currentEndTime;
    task.addEventListener('click', () => openTaskModal(taskName, taskDescription, timeStart, timeEnd, startDate, endDate, id, currentTaskListLength - currentTaskOffset, dayDifference, taskColor));

    taskList.push(task);
    scheduler.appendChild(task);
}

// Function to open the task-modal and pre-fill it with task data
function openTaskModal(taskName, taskDescription, startTime, endTime, startDate, endDate,id, taskIndex, dayDifference, taskColor) {
    const modal = document.getElementById('task-modal-update');

    currentId = id;
    console.log(startDate + " " + endDate);
    currentTaskIndex = taskIndex;
    currentTaskDayDiff = dayDifference;

    // Pre-fill modal fields with task data
    document.getElementById('task-name-update').value = taskName;
    document.getElementById('start-time-update').value = startTime;
    document.getElementById('end-time-update').value = endTime;
    document.getElementById('start-date-update').value = startDate;
    document.getElementById('end-date-update').value = endDate;
    document.getElementById('task-description-update').value = taskDescription;

    selectButtonByColor(taskColor);

    // Show the modal
    modal.style.display = 'block';

    // Add blur to the background
    document.body.classList.add('modal-open');
}

function selectButtonByColor(colorHex) {
    const buttons = document.querySelectorAll('.color-btn-update');

    // Iterate through buttons to find the one with matching data-color
    buttons.forEach(button => {
        if (button.getAttribute('data-color') === colorHex) {
            // Add the 'selected' class to the matching button
            selectedTaskColorUpdate = colorHex;
            button.classList.add('selected');
        } else {
            button.classList.remove('selected');
        }
    });
}

function closeTaskModal() {
    const modal = document.getElementById('task-modal-update');

    document.getElementById('task-name-update').value = '';
    document.getElementById('start-time-update').value = '';
    document.getElementById('end-time-update').value = '';
    document.getElementById('start-date-update').value = '';
    document.getElementById('end-date-update').value = '';
    document.getElementById('task-description-update').value = '';

    modal.style.display = 'none';

    document.body.classList.remove('modal-open');
}

function closeTaskModalNormal() {
    const modal = document.getElementById('task-modal');

    document.getElementById('task-name-update').value = '';
    document.getElementById('start-time-update').value = '';
    document.getElementById('end-time-update').value = '';
    document.getElementById('start-date-update').value = '';
    document.getElementById('end-date-update').value = '';
    document.getElementById('task-description-update').value = '';

    modal.style.display = 'none';

    document.body.classList.remove('modal-open');
    console.log("window closed");
}

async function deleteTask(id, index, dayDifference) {
    console.log("Index(length): " + index + " day diff: " + dayDifference);

    const taskData = {
        id: id
    };

    try {
        const response = await fetchWithAuth(`${BASE_API_URL}/task/deleteTask`, { // Using BASE_API_URL
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(taskData)
        });

        if (!response.ok) {
            throw new Error('Failed to delete the task.');
        }

        const data = await response.json();

        if (data.message === 'Success') {
            for (let i = index; i <= index + dayDifference; i++) {
                console.log("Removing");
                taskList[i].remove();
            }
        } else {
            alert('Failed to delete the task on the server.');
        }
    } catch (error) {
        alert('Error connecting to the server: ' + error.message);
    }
}



// Calculate task height for the visual scheduler
function calculateTaskHeight(taskTop, endHours, endMinutes, hourHeight, hourDivs) {
    let taskHeightHours= hourDivs[endHours].offsetTop - taskTop;
    return taskHeightHours + ((endMinutes/60)*hourHeight);

}

function clearTaskList() {
    taskList.length = 0;
}

function showSuccessPopup() {
    successPopup.classList.add('show');
    setTimeout(() => {
        successPopup.classList.remove('show');
    }, 3000); // Popup will disappear after 3 seconds
}


document.getElementById('add-task-btn').addEventListener('click', addTask);
document.getElementById('add-task-btn-update').addEventListener('click', updateTask);
document.getElementById('close-modal-update').addEventListener('click', () => closeTaskModal());
