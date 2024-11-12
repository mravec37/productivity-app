
// Wait for the page to load
/*window.addEventListener('load', () => {
  const contentDiv = document.getElementById('contento');

  // Create a new paragraph element
  const newParagraph = document.createElement('p');
  newParagraph.textContent = 'Hello, World!';

  // Add the new paragraph to the content div
  contentDiv.appendChild(newParagraph);
});*/
// Function that will run after setting the date
let chosenDate;  // Define chosenDate in the global scope

function onDateSet() {
    const startDate = chosenDate;
    const endDate = getEndDate(startDate);  // Get the end date (4 days later)

    const url = `http://192.168.1.36:8080/task/getTasks?startDate=${startDate}&endDate=${endDate}`;

    fetch(url, {
        method: 'GET',
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => {
        console.log("Response from server:", data.message);
    })
    .catch(error => {
        console.error('Error calling getTasks API:', error);
    });
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

// Run the setChosenDate function when the page loads
window.addEventListener('load', setChosenDate);
