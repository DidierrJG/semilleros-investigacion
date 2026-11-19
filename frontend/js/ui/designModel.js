/**
 * Modelo de Diseño Institucional UFPS - Semilleros de Investigación
 *
 * Este módulo centraliza las funciones generadoras y constantes del sistema de
 * diseño para garantizar consistencia visual y estructural en todas las vistas:
 *
 * Estructura estándar de cada vista:
 * 1. <div class="page-header"> -> Título, subtítulo y botones de acción a la derecha (.page-actions)
 * 2. <div class="kpi-grid">    -> Indicadores clave con borde rojo (.kpi-card)
 * 3. <div class="toolbar">     -> Barra de búsqueda (.search-box), filtros (.filter-select) y switch de vista (.view-toggle)
 * 4. Contenedor de datos       -> Tarjetas (.cards-grid > .semillero-card) o Tabla (.table-card > .data-table)
 */

import { escapeHtml } from './components.js';

/**
 * Genera el encabezado institucional de la página.
 */
export function createPageHeader({ title, subtitle, actionButtonHtml = '' }) {
  return `
    <div class="page-header">
      <div>
        <h1 class="page-title">${escapeHtml(title)}</h1>
        ${subtitle ? `<p class="page-subtitle">${escapeHtml(subtitle)}</p>` : ''}
      </div>
      ${actionButtonHtml ? `<div class="page-actions">${actionButtonHtml}</div>` : ''}
    </div>
  `;
}

/**
 * Genera una tarjeta de KPI institucional.
 */
export function createKpiCard({
  label,
  value,
  hint = '',
  isClickable = false,
  isActive = false,
  id = '',
  badgeHtml = '',
}) {
  const clickableClass = isClickable ? 'is-clickable' : '';
  const activeClass = isActive ? 'is-active' : '';
  const idAttr = id ? `id="${escapeHtml(id)}"` : '';
  const roleAttr = isClickable ? 'role="button" tabindex="0"' : '';

  return `
    <div class="kpi-card ${clickableClass} ${activeClass}" ${idAttr} ${roleAttr}>
      <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px;">
        <span class="kpi-label">${escapeHtml(label)}</span>
        ${badgeHtml}
      </div>
      <span class="kpi-value">${escapeHtml(String(value))}</span>
      ${hint ? `<span class="kpi-hint">${escapeHtml(hint)}</span>` : ''}
    </div>
  `;
}

/**
 * Envuelve una lista de tarjetas en el grid de KPIs estándar.
 */
export function createKpiGrid(cardsHtml = '') {
  return `<div class="kpi-grid">${cardsHtml}</div>`;
}

/**
 * Genera la barra de herramientas institucional (búsqueda, filtros y toggle).
 */
export function createToolbar({
  searchId = '',
  searchPlaceholder = 'Buscar...',
  searchValue = '',
  filtersHtml = '',
  viewToggleHtml = '',
  extraControlsHtml = '',
}) {
  return `
    <div class="toolbar">
      <div class="search-box">
        <span class="search-box__icon">🔍</span>
        <input
          type="search"
          class="search-box__input"
          ${searchId ? `id="${escapeHtml(searchId)}"` : ''}
          placeholder="${escapeHtml(searchPlaceholder)}"
          value="${escapeHtml(searchValue)}"
        />
      </div>
      ${filtersHtml}
      ${extraControlsHtml}
      ${viewToggleHtml}
    </div>
  `;
}

/**
 * Genera el control de alternancia Tarjetas / Tabla.
 */
export function createViewToggle(activeMode = 'table') {
  return `
    <div class="view-toggle" role="group" aria-label="Modo de visualización">
      <button type="button" class="view-toggle__btn ${activeMode === 'cards' ? 'is-active' : ''}" data-view-mode="cards">Tarjetas</button>
      <button type="button" class="view-toggle__btn ${activeMode === 'table' ? 'is-active' : ''}" data-view-mode="table">Tabla</button>
    </div>
  `;
}

/**
 * Genera la barra de progreso institucional para porcentaje de cumplimiento.
 */
export function createProgressBar(percentage = 0) {
  const safePercent = Math.min(100, Math.max(0, Number(percentage) || 0));
  const isComplete = safePercent === 100;
  const barColor = isComplete ? '#16a34a' : 'var(--ufps-red-600)';

  return `
    <div style="display: flex; align-items: center; gap: 10px;">
      <div style="flex: 1; height: 8px; background: var(--border-color); border-radius: 4px; overflow: hidden;">
        <div style="width: ${safePercent}%; height: 100%; background: ${barColor};"></div>
      </div>
      <strong style="min-width: 44px; font-size: 0.88rem; color: ${isComplete ? '#16a34a' : 'var(--text-primary)'};">
        ${safePercent}%
      </strong>
    </div>
  `;
}
