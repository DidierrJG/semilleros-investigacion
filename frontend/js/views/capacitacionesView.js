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
      (c.nombreCapacitacion || '').toLowerCase().includes(q) ||
      (c.objetivoCapacitacion || '').toLowerCase().includes(q) ||
      (c.semilleroSigla || '').toLowerCase().includes(q) ||
      (c.semilleroNombre || '').toLowerCase().includes(q);

    const matchesSemillero =
      !filters.semilleroId || Number(c.semilleroId) === Number(filters.semilleroId);

    return matchesQuery && matchesSemillero;
  });

  const totalParticipantes = capacitaciones.reduce(
    (acc, cur) => acc + (cur.cantidadParticipantes || 0),
    0
  );
  const promedioCumplimiento = capacitaciones.length
    ? Math.round(
        capacitaciones.reduce((acc, cur) => acc + (cur.porcentajeCumplimiento || 0), 0) /
          capacitaciones.length
      )
    : 0;

  const semillerosConCapacitacion = new Set(
    capacitaciones.map((c) => Number(c.semilleroId)).filter(Boolean)
  ).size;

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
              <th>Capacitación</th>
              <th>Semillero</th>
              <th>Fecha</th>
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
                  <div>
                    <strong>${escapeHtml(c.nombreCapacitacion)}</strong>
                    ${
                      c.objetivoCapacitacion
                        ? `<div class="text-sm text-muted" style="margin-top: 4px; max-width: 440px; line-height: 1.4;">${escapeHtml(c.objetivoCapacitacion)}</div>`
                        : ''
                    }
                  </div>
                </td>
                <td>
                  <button type="button" class="semillero-link-btn" data-view-semillero="${c.semilleroId}" title="Ver semillero">
                    <span>${escapeHtml(c.semilleroSigla || 'SEM')}</span>
                  </button>
                </td>
                <td>
                  <span class="text-sm" style="font-weight: 500;">📅 ${escapeHtml(c.fecha || 'Sin fecha')}</span>
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
        <article class="semillero-card">
          <div class="semillero-card__header">
            <button type="button" class="sigla-badge" style="border: 0; cursor: pointer;" data-view-semillero="${c.semilleroId}" title="Ver información del semillero">
              ${escapeHtml(c.semilleroSigla || 'SEM')}
            </button>
            <span class="badge badge--member">👥 ${c.cantidadParticipantes} asist.</span>
          </div>

          <div>
            <h2 class="semillero-card__title">${escapeHtml(c.nombreCapacitacion)}</h2>
            <p class="semillero-card__group">📅 ${escapeHtml(c.fecha || 'Sin fecha')}</p>
          </div>

          <hr class="card-divider" />

          <p class="card__desc" style="font-size: 0.88rem; color: var(--text-secondary); line-height: 1.45; margin: 0;">
            ${escapeHtml(c.objetivoCapacitacion || 'Sin objetivo registrado')}
          </p>

          <div class="semillero-card__meta">
            <div class="meta-row" style="flex-direction: column; gap: 6px;">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span class="meta-row__label">% Cumplimiento:</span>
                <strong style="color: ${c.porcentajeCumplimiento === 100 ? '#16a34a' : 'var(--ufps-red-600)'};">
                  ${c.porcentajeCumplimiento}%
                </strong>
              </div>
              <div style="height: 8px; background: var(--border-color); border-radius: 4px; overflow: hidden;">
                <div style="width: ${Math.min(100, Math.max(0, c.porcentajeCumplimiento))}%; height: 100%; background: ${c.porcentajeCumplimiento === 100 ? '#16a34a' : 'var(--ufps-red-600)'};"></div>
              </div>
            </div>
          </div>

          <div class="semillero-card__footer">
            <button type="button" class="btn btn--soft-red btn--sm" data-view-semillero="${c.semilleroId}">Ver semillero</button>
            <div class="card-actions">
              <button type="button" class="btn btn--secondary btn--sm" data-edit-capacitacion="${c.id}">Editar</button>
              <button type="button" class="btn btn--danger btn--sm" data-delete-capacitacion="${c.id}">Eliminar</button>
            </div>
          </div>
        </article>`
        )
        .join('')}
    </div>
  `;

  const contentHtml = filtered.length
    ? viewMode === 'table'
      ? tableHtml
      : cardsHtml
    : renderEmptyState(
        'No se encontraron capacitaciones',
        capacitaciones.length
          ? 'Intenta ajustar los filtros de búsqueda para ver más resultados.'
          : 'Aún no se han registrado capacitaciones en el sistema.',
        `<button type="button" class="btn btn--primary" id="emptyNewCapacitacionBtn">+ Registrar Primera Capacitación</button>`
      );

  container.innerHTML = `
    <section>
      <div class="page-header">
        <div>
          <h1 class="page-title">Capacitaciones de Investigación</h1>
          <p class="page-subtitle">Talleres formativos, cursos y sesiones de capacitación técnica organizadas por los semilleros.</p>
        </div>
        <div class="page-actions">
          <button type="button" class="btn btn--primary" id="newCapacitacionBtn">+ Nueva Capacitación</button>
        </div>
      </div>

      <div class="kpi-grid">
        <div class="kpi-card">
          <span class="kpi-label">Total Capacitaciones</span>
          <span class="kpi-value">${capacitaciones.length}</span>
          <span class="kpi-hint">Sesiones formativas registradas</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">Participantes Formados</span>
          <span class="kpi-value">${totalParticipantes}</span>
          <span class="kpi-hint">Estudiantes y docentes capacitados</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">Promedio Cumplimiento</span>
          <span class="kpi-value">${promedioCumplimiento}%</span>
          <span class="kpi-hint">Efectividad de sesiones</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">Semilleros Activos</span>
          <span class="kpi-value">${semillerosConCapacitacion}</span>
          <span class="kpi-hint">Organizando capacitaciones</span>
        </div>
      </div>

      <div class="toolbar">
        <div class="search-box">
          <span class="search-box__icon">🔍</span>
          <input
            type="search"
            class="search-box__input"
            id="searchCapacitaciones"
            placeholder="Buscar por nombre, objetivo o semillero..."
            value="${escapeHtml(filters.query)}"
          />
        </div>

        <select class="filter-select" id="filterSemillero" aria-label="Filtrar por semillero">
          <option value="">Todos los semilleros</option>
          ${semilleroOptions}
        </select>

        <div class="view-toggle" role="group" aria-label="Modo de visualización">
          <button type="button" class="view-toggle__btn ${viewMode === 'cards' ? 'is-active' : ''}" data-view-mode="cards">Tarjetas</button>
          <button type="button" class="view-toggle__btn ${viewMode === 'table' ? 'is-active' : ''}" data-view-mode="table">Tabla</button>
        </div>
      </div>

      ${contentHtml}
    </section>
  `;

  // Event Listeners
  const searchInput = container.querySelector('#searchCapacitaciones');
  searchInput?.addEventListener('input', (e) => {
    filters.query = e.target.value;
    renderCapacitacionesView(container);
    const nextInput = container.querySelector('#searchCapacitaciones');
    if (nextInput) {
      nextInput.focus();
      nextInput.setSelectionRange(nextInput.value.length, nextInput.value.length);
    }
  });

  const semSelect = container.querySelector('#filterSemillero');
  semSelect?.addEventListener('change', (e) => {
    filters.semilleroId = e.target.value;
    renderCapacitacionesView(container);
  });

  container.querySelectorAll('[data-view-mode]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const nextMode = btn.dataset.viewMode;
      if (nextMode && nextMode !== viewMode) {
        viewMode = nextMode;
        renderCapacitacionesView(container);
      }
    });
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
      const c = capacitaciones.find(
        (item) => Number(item.id) === Number(btn.dataset.editCapacitacion)
      );
      if (c) {
        openCapacitacionFormModal({ capacitacion: c }, () =>
          renderCapacitacionesView(container)
        );
      }
    });
  });

  // Eliminar capacitación
  container.querySelectorAll('[data-delete-capacitacion]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const c = capacitaciones.find(
        (item) => Number(item.id) === Number(btn.dataset.deleteCapacitacion)
      );
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
