/* Shared schedule calculations: browser, compatibility API and isolated tests. */
(function installPacScheduleCore(root, factory) {
  const api = factory();
  if (typeof module === 'object' && module.exports) module.exports = api;
  root.PacScheduleCore = api;
})(globalThis, function pacScheduleFactory() {
  'use strict';
  const DAY = 86400000;
  const HOUR = 3600000;
  const DAYS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];
  const caracasFormat = new Intl.DateTimeFormat('en-US', {
    timeZone: 'America/Caracas', year: 'numeric', month: 'numeric', day: 'numeric',
    hour: 'numeric', minute: 'numeric', second: 'numeric', hourCycle: 'h23'
  });

  function civilParts(timestamp) {
    const values = Object.fromEntries(caracasFormat.formatToParts(new Date(timestamp)).filter(p => p.type !== 'literal').map(p => [p.type, Number(p.value)]));
    const date = new Date(Date.UTC(values.year, values.month - 1, values.day));
    return { ...values, dayIndex: (date.getUTCDay() + 6) % 7 };
  }

  function civilEpoch(year, month, day, hour = 0) {
    // Resolve the IANA offset at this date instead of using the server/device timezone.
    const wall = Date.UTC(year, month - 1, day, hour);
    const guess = civilParts(wall);
    const offset = Date.UTC(guess.year, guess.month - 1, guess.day, guess.hour, guess.minute, guess.second) - wall;
    return wall - offset;
  }

  function normalizeBlock(value) {
    const clean = String(value ?? '').trim().toUpperCase().replace(/^BLOQUE\s*/, '');
    return /^[ABCD]$/.test(clean) ? clean : '-';
  }

  function weekOffset(day) { return Math.min(3, Math.max(0, Math.floor((day - 1) / 7))); }

  function editorOffset(nowMillis = Date.now()) { return weekOffset(civilParts(nowMillis).day); }

  function nextRevision(previous, nowMillis = Date.now()) {
    const values = [previous?.version, previous?.updatedAt].map(value => Number(value) || Date.parse(value) || 0);
    return Math.max(nowMillis, ...values.map(value => value + 1));
  }

  function rotateMatrix(matrix, shift) {
    if (!Array.isArray(matrix) || !matrix.length) return [];
    return matrix.map((_, i) => [...matrix[((i + shift) % matrix.length + matrix.length) % matrix.length]]);
  }

  function validateSchedule(value) {
    if (!value || typeof value !== 'object') throw new Error('Cronograma no disponible.');
    const source = value.matrixRows?.map(row => row?.cells) || value.matrix;
    if (!Array.isArray(source) || !source.length || source.length > 24 || !Array.isArray(value.slots) || value.slots.length !== source.length) throw new Error('Filas y franjas incompatibles.');
    const matrix = source.map(row => {
      if (!Array.isArray(row) || row.length !== 7 || row.some(cell => typeof cell !== 'string' || !/^(?:[ABCD-]|Bloque\s*[ABCD])$/i.test(cell.trim()))) throw new Error('Matriz PAC inválida.');
      return row.map(normalizeBlock);
    });
    const slots = value.slots.map((slot, index) => {
      if (!slot || typeof slot.timeLabel !== 'string' || slot.timeLabel.length > 100 || !Number.isInteger(slot.startHour) || !Number.isInteger(slot.endHour)
          || slot.startHour < 0 || slot.startHour > 23 || slot.endHour < 0 || slot.endHour > 24 || slot.startHour === slot.endHour || (slot.startHour === 0 && slot.endHour === 24)) throw new Error('Franja PAC inválida.');
      return { ...slot, index };
    });
    return { ...value, slots, matrix, matrixRows: matrix.map(cells => ({ cells })) };
  }

  function windows(config, nowMillis, fromDay = -1, toDay = 14) {
    const schedule = validateSchedule(config);
    const today = civilParts(nowMillis);
    const rows = [];
    for (let delta = fromDay; delta <= toDay; delta++) {
      const date = new Date(Date.UTC(today.year, today.month - 1, today.day + delta));
      const year = date.getUTCFullYear(), month = date.getUTCMonth() + 1, day = date.getUTCDate();
      const dayIndex = (date.getUTCDay() + 6) % 7;
      schedule.slots.forEach((slot, index) => {
        const startMillis = civilEpoch(year, month, day, slot.startHour);
        let endMillis = civilEpoch(year, month, day, slot.endHour);
        if (endMillis <= startMillis) endMillis += DAY;
        rows.push({ ...slot, slotIndex: index, dayIndex, dayOfMonth: day, dayOffset: delta,
          block: schedule.matrix[(index + weekOffset(day)) % schedule.matrix.length][dayIndex],
          startMillis, endMillis, isCurrentlyActive: nowMillis >= startMillis && nowMillis < endMillis,
          dayName: DAYS[dayIndex], hoursUntil: Math.max(0, (startMillis - nowMillis) / HOUR) });
      });
    }
    return rows.sort((a, b) => a.startMillis - b.startMillis);
  }

  function findWindow(config, block, nowMillis = Date.now()) {
    const clean = normalizeBlock(block);
    if (clean === '-') return null;
    return windows(config, nowMillis).find(w => w.block === clean && (w.isCurrentlyActive || w.startMillis > nowMillis)) || null;
  }

  function monthlyWeeks(config, monthName, totalDays) {
    const schedule = validateSchedule(config);
    if (typeof monthName !== 'string' || !monthName.trim() || monthName.length > 40 || !Number.isInteger(Number(totalDays)) || Number(totalDays) < 28 || Number(totalDays) > 31) throw new Error('Mes o número de días inválido.');
    return [0, 1, 2, 3].map(offset => ({ weekNumber: offset + 1,
      dateRangeLabel: `DEL ${['01', '08', '15', '22'][offset]} AL ${offset === 3 ? Number(totalDays) : ['07', '14', '21'][offset]} DE ${monthName.trim().toUpperCase()}`,
      matrix: rotateMatrix(schedule.matrix, offset) }));
  }

  function pacTime(slots, matrix, nowMillis = Date.now()) {
    const today = civilParts(nowMillis);
    let all = [];
    try { all = windows({ slots, matrix }, nowMillis); } catch { /* Explicit unknown state preserves the last valid data in the caller. */ }
    const active = all.find(w => w.isCurrentlyActive);
    const next = all.find(w => w.startMillis > nowMillis);
    const blank = { timeLabel: 'Sin franja activa', startHour: 0, endHour: 0 };
    const totalSeconds = Math.max(0, Math.floor(((active?.endMillis ?? next?.startMillis ?? nowMillis) - nowMillis) / 1000));
    const hours = Math.floor(totalSeconds / 3600), minutes = Math.floor(totalSeconds % 3600 / 60), seconds = totalSeconds % 60;
    const date = new Date(today.year, today.month - 1, today.day, today.hour, today.minute, today.second);
    return { caracasDate: date, dayIndex: today.dayIndex, dayName: DAYS[today.dayIndex],
      weekShift: weekOffset(today.day), activeSlotIdx: active?.slotIndex ?? -1, activeSlot: active ?? blank,
      activeSlotIsToday: active?.dayOffset === 0, activeCutBlock: active?.block ?? '-',
      nextSlotIdx: next?.slotIndex ?? -1, nextSlot: next ?? blank, nextCutBlock: next?.block ?? '-',
      todayBlocks: slots.map((_, index) => all.find(w => w.dayOffset === 0 && w.slotIndex === index)?.block ?? '-'),
      countdown: { hours, minutes, seconds, totalSeconds, formatted: `${String(hours).padStart(2, '0')}h ${String(minutes).padStart(2, '0')}m ${String(seconds).padStart(2, '0')}s` } };
  }

  return { civilParts, civilEpoch, normalizeBlock, weekOffset, editorOffset, nextRevision, rotateMatrix, validateSchedule, windows, findWindow, monthlyWeeks, pacTime };
});
