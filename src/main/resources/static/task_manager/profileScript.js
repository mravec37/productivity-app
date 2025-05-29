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
    const refreshResponse = await fetch("http://localhost:8080/auth/refresh-token", {
      method: "POST",
      credentials: "include"
    });

    if (!refreshResponse.ok) {
      console.warn("Platnosť refresh tokenu vypršala alebo je neplatný");
      localStorage.removeItem("jwtToken");
      window.location.href = "./index.html";
      return;
    }

    const { accessToken } = await refreshResponse.json();
    localStorage.setItem("jwtToken", accessToken);

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

document.addEventListener('DOMContentLoaded', () => {
  const menuItems = document.querySelectorAll('.menu ul li');

  menuItems.forEach((item, index) => {
    item.addEventListener('click', () => {
      if (index === 0) {
        window.location.href = './index.html';
      } else if (index === 1) {
        location.reload();
      }
    });
  });
});

document.addEventListener('DOMContentLoaded', async () => {
  const token = localStorage.getItem("jwtToken");
  if (!token) {
    document.getElementById('usernameDisplay').textContent = "Hosť";
    return;
  }

  try {
    const response = await fetchWithAuth("http://localhost:8080/users/getUsername", { method: "GET" });

    if (!response.ok) throw new Error("Nepodarilo sa získať používateľské meno");

    const username = await response.text();
    document.getElementById('usernameDisplay').textContent = username || "žiadny";
  } catch (error) {
    console.error("Chyba pri načítaní mena:", error);
    document.getElementById('usernameDisplay').textContent = "Neznámy používateľ";
  }
});

document.addEventListener('DOMContentLoaded', async () => {
  const summaryDiv = document.getElementById("taskSummary");
  const token = localStorage.getItem("jwtToken");

  if (!token) {
    summaryDiv.textContent = "Nie ste prihlásený. Prosím, prihláste sa.";
    return;
  }

  try {
    const response = await fetchWithAuth("http://localhost:8080/task/getTotalTaskTime");

    if (!response.ok) {
      const errorText = await response.text();
      summaryDiv.textContent = `Nepodarilo sa načítať čas úloh: ${errorText}`;
      return;
    }

    const totalHours = await response.json();
    summaryDiv.innerHTML += `<p><strong>Celkový počet hodín splnených úloh:</strong> <span class="highlight-number">${totalHours ?? 'žiadny'}</span> h</p>`;
  } catch (error) {
    summaryDiv.textContent = `Chyba: ${error.message}`;
  }
});

document.addEventListener('DOMContentLoaded', async () => {
  const summaryDiv = document.getElementById('taskSummary');
  const token = localStorage.getItem('jwtToken');

  if (!token) {
    window.location.href = 'index.html';
    return;
  }

  try {
    const response = await fetchWithAuth('http://localhost:8080/task/doneAndPlannedTasks');

    if (!response.ok) {
      window.location.href = 'index.html';
      return;
    }

    const { tasksDone, tasksPlanned } = await response.json();

    summaryDiv.innerHTML += `
      <p><strong>Dokončené úlohy:</strong> <span class="highlight-number">${tasksDone ?? 'žiadne'}</span></p>
      <p><strong>Naplánované úlohy:</strong> <span class="highlight-number">${tasksPlanned ?? 'žiadne'}</span></p>
    `;
  } catch (error) {
    console.error('Chyba pri načítaní údajov o úlohách:', error);
    summaryDiv.textContent = 'Nepodarilo sa načítať prehľad úloh.';
  }
});

document.addEventListener("DOMContentLoaded", async () => {
  const summaryDiv = document.getElementById("longestTaskSummary");
  const token = localStorage.getItem("jwtToken");

  if (!token) {
    summaryDiv.innerHTML = "<p>Žiadny token. Prihláste sa prosím.</p>";
    return;
  }

  try {
    const response = await fetchWithAuth("http://localhost:8080/task/getLongestTask");

    if (!response.ok) throw new Error("Nepodarilo sa získať najdlhšiu úlohu");

    const data = await response.json();

    summaryDiv.innerHTML += `
      <p><strong>Názov úlohy:</strong> ${data.taskName ?? 'žiadny'}</p>
      <p><strong>Začiatok:</strong> ${data.startDate ?? 'žiadny'}</p>
      <p><strong>Koniec:</strong> ${data.endDate ?? 'žiadny'}</p>
      <p><strong>Trvanie (h):</strong> <span class="highlight-number">${data.durationHours ?? 'žiadne'}</span></p>
    `;
  } catch (error) {
    console.error("Chyba pri načítaní najdlhšej úlohy:", error);
    summaryDiv.innerHTML = "<p>Chyba pri načítaní údajov o najdlhšej úlohe.</p>";
  }
});

document.addEventListener('DOMContentLoaded', async () => {
  const peakTaskDiv = document.getElementById('peakTaskDayInfo');
  const token = localStorage.getItem('jwtToken');

  if (!token || !peakTaskDiv) return;

  try {
    const response = await fetchWithAuth('http://localhost:8080/task/peakTaskDay');

    if (!response.ok) throw new Error('Nepodarilo sa získať deň s najviac úlohami');

    const data = await response.json();

    peakTaskDiv.innerHTML += `
      <p><strong>Najaktívnejší deň:</strong> ${data.date ?? 'žiadny'}</p>
      <p><strong>Počet úloh:</strong> <span class="highlight-number">${data.taskCount ?? 'žiadne'}</span></p>
    `;
  } catch (error) {
    console.error('Chyba:', error);
    peakTaskDiv.innerHTML += `<p style="color:red;">Nepodarilo sa načítať informácie o najaktívnejšom dni.</p>`;
  }
});

document.addEventListener("DOMContentLoaded", () => {
  const logoutBtn = document.getElementById("logoutBtn");

  if (logoutBtn) {
    logoutBtn.addEventListener("click", () => {
      localStorage.removeItem("jwtToken");
      document.cookie = "refreshToken=; Max-Age=0; path=/; secure; sameSite=Strict";
      window.location.href = "./index.html";
    });
  }
});
