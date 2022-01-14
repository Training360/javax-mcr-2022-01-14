window.onload = function() {
    let evtSource = new EventSource("api/messages");
    evtSource.addEventListener("message", event => handleEvent(event));
    getEmployeeById()
}

function handleEvent(event) {
    console.log(event)
    getEmployeeById()
}

async function getEmployeeById() {
    const queryString = window.location.search
    const urlParams = new URLSearchParams(queryString)
    const id = urlParams.get("id")
    const response = await fetch(`/api/employees/${id}`)
    const employee = await response.json()
    const div = document.querySelector("#employee-div")
    const content = `Name: ${employee.name} Hours: ${employee.hours}`
    div.innerHTML = content
}

