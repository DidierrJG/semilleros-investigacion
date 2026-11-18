import { store, loadAllData, getSemilleroById } from '../state/store.js';
import { capacitacionesApi } from '../api/capacitacionesApi.js';
import { confirmDialog } from '../ui/modal.js';
import { showToast } from '../ui/toast.js';
import {
  escapeHtml,
  openCapacitacionFormModal,
  openSemilleroPreviewModal,
  renderEmptyState,
} from '../ui/components.js';

let filters = {
  query: '',
  semilleroId: '',
};

let viewMode = 'table'; // 'table' | 'cards'

export function renderCapacitacionesView(container) {
  const { capacitaciones, semilleros } = store;

  const filtered = capacitaciones.filter((c) => {
    const q = filters.query.trim().toLowerCase();
    const matchesQuery =
      !q ||
      c.nombreCapacitacion.toLowerCase().includes(q) ||
      (c.objetivoCapacitacion || '').toLowerCase().includes(q) ||
      (c.semilleroSigla || '').toLowerCase().includes(q) ||
      (c.semilleroNombre || '').toLowerCase().includes(q);

    const matchesSemillero =
      !filters.semilleroId || Number(c.semilleroId) === Number(filters.semilleroId);

    return matchesQuery && matchesSemillero;
  });

  const totalParticipantes = capacitaciones.reduce((acc, cur) => acc + (cur.cantidadParticipantes || 0), 0);
  const promedioCumplimiento = capacitaciones.length
    ? Math.round(capacitaciones.reduce((acc, cur) => acc + (cur.porcentajeCumplimiento || 0), 0) / capacitaciones.length)
    : 0;

  const semilleroOptions = semilleros
    .map(
      (s) =>
        `<option value="${s.id}" ${Number(filters.semilleroId) === Number(s.id) ? 'selected' : ''}>${escapeHtml(s.sigla)} — ${escapeHtml(s.nombre)}</option>`
    )
    .join('');

  const tableHtml = `
    <div class="table-card">
      <div class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th>Nombre de la Capacitación</th>
              <th>Semillero</th>
              <th>Fecha</th>
              <th>Objetivo</th>
              <th style="width: 140px;">Participantes</th>
              <th style="width: 200px;">% Cumplimiento</th>
              <th style="text-align: right;">Acciones</th>
            </tr>
          </thead>
          <tbody>
            ${filtered
              .map(
                (c) => `
              <tr>
                <td>
                  <strong>${escapeHtml(c.nombreCapacitacion)}</strong>
                </td>
                <td>
                  <button type="button" class="semillero-link-btn" data-view-semillero="${c.semilleroId}" title="Ver semillero">
                    <span>${escapeHtml(c.semilleroSigla || 'SEM')}</span>
                  </button>
                </td>
                <td>
                  <span class="text-sm text-muted">${escapeHtml(c.fecha || '')}</span>
                </td>
                <td>
                  <div style="max-width: 260px; font-size: 0.88rem; color: var(--text-secondary); white-space: normal; line-height: 1.4;">
                    ${escapeHtml(c.objetivoCapacitacion || '')}
                  </div>
                </td>
                <td>
                  <span class="badge badge--member">👥 ${c.cantidadParticipantes} pers.</span>
                </td>
                <td>
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <div style="flex: 1; height: 8px; background: var(--border-color); border-radius: 4px; overflow: hidden;">
                      <div style="width: ${Math.min(100, Math.max(0, c.porcentajeCumplimiento))}%; height: 100%; background: ${c.porcentajeCumplimiento === 100 ? '#16a34a' : 'var(--ufps-red-600)'};"></div>
                    </div>
                    <strong style="min-width: 44px; font-size: 0.88rem; color: ${c.porcentajeCumplimiento === 100 ? '#16a34a' : 'var(--text-primary)'};">
                      ${c.porcentajeCumplimiento}%
                    </strong>
                  </div>
                </td>
                <td style="text-align: right;">
                  <div class="card-actions" style="justify-content: flex-end;">
                    <button type="button" class="btn btn--secondary btn--sm" data-edit-capacitacion="${c.id}">Editar</button>
                    <button type="button" class="btn btn--danger btn--sm" data-delete-capacitacion="${c.id}">Eliminar</button>
                  </div>
                </td>
              </tr>`
              )
              .join('')}
          </tbody>
        </table>
      </div>
    </div>
  `;

  const cardsHtml = `
    <div class="cards-grid">
      ${filtered
        .map(
          (c) => `
        <article class="resource-card">
          <header class="resource-card__header">
            <div style="display: flex; gap: 6px; align-items: center; flex-wrap: wrap;">
              <button type="button" class="semillero-link-btn" data-view-semillero="${c.semilleroId}" title="Ver semillero">
                <span>${escapeHtml(c.semilleroSigla || 'SEM')}</span>
              </button>
              <span class="text-xs text-muted">📅 ${escapeHtml(c.fecha || '')}</span>
            </div>
            <span class="badge badge--member">👥 ${c.cantidadParticipantes} asist.</span>
          </header>

          <h3 class="resource-card__title" style="margin-top: 10px;">${escapeHtml(c.nombreCapacitacion)}</h3>

          <p class="resource-card__desc" style="margin: 10px 0 14px; font-size: 0.9rem; line-height: 1.5; color: var(--text-secondary);">
            ${escapeHtml(c.objetivoCapacitacion || '')}
          </p>

          <div style="margin-bottom: 14px;">
            <div style="display: flex; justify-content: space-between; font-size: 0.82rem; margin-bottom: 4px;">
              <span class="text-muted">Cumplimiento</span>
              <strong style="color: ${c.porcentajeCumplimiento === 100 ? '#16a34a' : 'var(--ufps-red-600)'};">${c.porcentajeCumplimiento}%</strong>
            </div>
            <div style="height: 6px; background: var(--border-color); border-radius: 3px; overflow: hidden;">
              <div style="width: ${Math.min(100, Math.max(0, c.porcentajeCumplimiento))}%; height: 100%; background: ${c.porcentajeCumplimiento === 100 ? '#16a34a' : 'var(--ufps-red-600)'};"></div>
            </div>
          </div>

          <footer class="resource-card__footer">
            <div class="card-actions">
              <button type="button" class="btn btn--secondary btn--sm" data-edit-capacitacion="${c.id}">Editar</button>
              <button type="button" class="btn btn--danger btn--sm" data-delete-capacitacion="${c.id}">Eliminar</button>
            </div>
          </footer>
        </article>`
        )
        .join('')}
    </div>
  `;

  container.innerHTML = `
    <div class="view-header">
      <div>
        <h1 class="view-title">Capacitaciones de Investigación</h1>
        <p class="view-subtitle">Talleres formativos, cursos y sesiones de capacitación técnica organizadas por los semilleros.</p>
      </div>
      <div class="view-actions">
        <button type="button" class="btn btn--primary" id="newCapacitacionBtn">
          + Nueva Capacitación
        </button>
      </div>
    </div>

    <!-- Barra de Métricas Rápidas -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 14px; margin-bottom: 20px;">
      <div style="background: var(--surface-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px 20px;">
        <span class="text-xs text-muted" style="text-transform: uppercase; font-weight: 700;">Total Capacitaciones</span>
        <div style="font-size: 1.6rem; font-weight: 800; color: var(--text-primary); margin-top: 4px;">${capacitaciones.length}</div>
      </div>
      <div style="background: var(--surface-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px 20px;">
        <span class="text-xs text-muted" style="text-transform: uppercase; font-weight: 700;">Participantes Formados</span>
        <div style="font-size: 1.6rem; font-weight: 800; color: #0284c7; margin-top: 4px;">👥 ${totalParticipantes}</div>
      </div>
      <div style="background: var(--surface-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px 20px;">
        <span class="text-xs text-muted" style="text-transform: uppercase; font-weight: 700;">Promedio Cumplimiento</span>
        <div style="font-size: 1.6rem; font-weight: 800; color: #16a34a; margin-top: 4px;">${promedioCumplimiento}%</div>
      </div>
    </div>

    <div class="filter-bar">
      <div class="search-input-wrapper">
        <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
        <input
          type="search"
          class="form-control"
          id="searchCapacitaciones"
          placeholder="Buscar por nombre, objetivo, semillero..."
          value="${escapeHtml(filters.query)}"
        />
      </div>

      <select class="form-control" id="filterSemillero" style="max-width: 260px;">
        <option value="">Todos los semilleros</option>
        ${semilleroOptions}
      </select>

      ${
        filters.query || filters.semilleroId
          ? `<button type="button" class="btn btn--secondary btn--sm" id="clearFiltersBtn">Limpiar filtros</button>`
          : ''
      }

      <div style="margin-left: auto; display: flex; gap: 4px; background: var(--surface-card); border: 1px solid var(--border-color); padding: 2px; border-radius: var(--radius-md);">
        <button type="button" class="btn btn--sm ${viewMode === 'table' ? 'btn--primary' : 'btn--secondary'}" id="toggleTableViewBtn" title="Vista de tabla">
          Tabla
        </button>
        <button type="button" class="btn btn--sm ${viewMode === 'cards' ? 'btn--primary' : 'btn--secondary'}" id="toggleCardsViewBtn" title="Vista de tarjetas">
          Tarjetas
        </button>
      </div>
    </div>

    ${
      filtered.length === 0
        ? renderEmptyState(
            'No se encontraron capacitaciones',
            filters.query || filters.semilleroId
              ? 'Intenta ajustar los filtros de búsqueda para ver más resultados.'
              : 'Aún no se han registrado capacitaciones en el sistema.',
            `<button type="button" class="btn btn--primary" id="emptyNewCapacitacionBtn">+ Registrar Primera Capacitación</button>`
          )
        : viewMode === 'table'
        ? tableHtml
        : cardsHtml
    }
  `;

  // Event Listeners
  const searchInput = container.querySelector('#searchCapacitaciones');
  searchInput?.addEventListener('input', (e) => {
    filters.query = e.target.value;
    renderCapacitacionesView(container);
  });

  const semSelect = container.querySelector('#filterSemillero');
  semSelect?.addEventListener('change', (e) => {
    filters.semilleroId = e.target.value;
    renderCapacitacionesView(container);
  });

  container.querySelector('#clearFiltersBtn')?.addEventListener('click', () => {
    filters.query = '';
    filters.semilleroId = '';
    renderCapacitacionesView(container);
  });

  container.querySelector('#toggleTableViewBtn')?.addEventListener('click', () => {
    viewMode = 'table';
    renderCapacitacionesView(container);
  });

  container.querySelector('#toggleCardsViewBtn')?.addEventListener('click', () => {
    viewMode = 'cards';
    renderCapacitacionesView(container);
  });

  const openNew = () => {
    openCapacitacionFormModal({}, () => renderCapacitacionesView(container));
  };
  container.querySelector('#newCapacitacionBtn')?.addEventListener('click', openNew);
  container.querySelector('#emptyNewCapacitacionBtn')?.addEventListener('click', openNew);

  // Ver semillero modal
  container.querySelectorAll('[data-view-semillero]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const s = getSemilleroById(btn.dataset.viewSemillero);
      if (s) openSemilleroPreviewModal(s);
    });
  });

  // Editar capacitación
  container.querySelectorAll('[data-edit-capacitacion]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const c = capacitaciones.find((item) => Number(item.id) === Number(btn.dataset.editCapacitacion));
      if (c) {
        openCapacitacionFormModal({ capacitacion: c }, () => renderCapacitacionesView(container));
      }
    });
  });

  // Eliminar capacitación
  container.querySelectorAll('[data-delete-capacitacion]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const c = capacitaciones.find((item) => Number(item.id) === Number(btn.dataset.deleteCapacitacion));
      if (!c) return;

      confirmDialog({
        title: 'Eliminar capacitación',
        message: `¿Estás seguro de que deseas eliminar la capacitación "${c.nombreCapacitacion}"?`,
        onConfirm: async () => {
          try {
            await capacitacionesApi.remove(c.id);
            await loadAllData();
            showToast('Capacitación eliminada correctamente', 'success');
            renderCapacitacionesView(container);
          } catch (err) {
            showToast(err.message, 'error');
            throw err;
          }
        },
      });
    });
  });
}
