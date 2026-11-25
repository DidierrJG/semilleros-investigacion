import { store, loadAllData, getSemilleroById } from '../state/store.js';
import { integrantesApi } from '../api/integrantesApi.js';
import { confirmDialog } from '../ui/modal.js';
import { showToast } from '../ui/toast.js';
import {
  escapeHtml,
  getInitials,
  openIntegranteFormModal,
  openIntegranteDetailModal,
  openSemilleroPreviewModal,
  renderEmptyState,
} from '../ui/components.js';

let filters = {
  query: '',
  semilleroId: '',
  programaId: '',
  soloLideres: false,
};

export function renderIntegrantesView(container) {
  const { integrantes, semilleros, programas } = store;

  const totalIntegrantes = integrantes.length;
  const totalLideres = integrantes.filter((i) => Boolean(i.esLider)).length;

  const filtered = integrantes.filter((i) => {
    const q = filters.query.trim().toLowerCase();
    const matchesQuery =
      !q ||
      i.nombre.toLowerCase().includes(q) ||
      i.codigo.toLowerCase().includes(q) ||
      (i.identificacion || '').toLowerCase().includes(q) ||
      (i.programaNombre || '').toLowerCase().includes(q) ||
      (i.semilleroNombre || '').toLowerCase().includes(q) ||
      (i.semilleroSigla || '').toLowerCase().includes(q) ||
      (i.email || '').toLowerCase().includes(q) ||
      (i.celular || '').toLowerCase().includes(q);

    const matchesSemillero =
      !filters.semilleroId || Number(i.semilleroId) === Number(filters.semilleroId);

    const matchesPrograma =
      !filters.programaId || Number(i.programaId) === Number(filters.programaId);

    const matchesLider = !filters.soloLideres || Boolean(i.esLider);

    return matchesQuery && matchesSemillero && matchesPrograma && matchesLider;
  });

  const semilleroOptions = semilleros
    .map(
      (s) =>
        `<option value="${s.id}" ${Number(filters.semilleroId) === Number(s.id) ? 'selected' : ''}>${escapeHtml(s.sigla)} — ${escapeHtml(s.nombre)}</option>`
    )
    .join('');

  const programaOptions = (programas || [])
    .map(
      (p) =>
        `<option value="${p.id}" ${Number(filters.programaId) === Number(p.id) ? 'selected' : ''}>${escapeHtml(p.nombre)}</option>`
    )
    .join('');

  const tableHtml = filtered.length
    ? `
      <div class="table-card">
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Integrante</th>
                <th>Código & Semestre</th>
                <th>Programa Académico</th>
                <th>Semillero (clic para ver info)</th>
                <th>Contacto & Edad</th>
                <th>Rol</th>
                <th style="text-align: right;">Acciones</th>
              </tr>
            </thead>
            <tbody>
              ${filtered
                .map(
                  (i) => `
                <tr>
                  <td>
                    <div class="person-cell">
                      <div class="avatar ${i.esLider ? 'avatar--leader' : ''}">${escapeHtml(getInitials(i.nombre))}</div>
                      <div>
                        <strong>${escapeHtml(i.nombre)}</strong>
                        ${
                          i.identificacion
                            ? `<div class="text-xs text-muted">ID: ${escapeHtml(i.identificacion)}</div>`
                            : ''
                        }
                      </div>
                    </div>
                  </td>
                  <td>
                    <div style="display: flex; align-items: center; gap: 6px;">
                      <code>${escapeHtml(i.codigo)}</code>
                      <span class="badge" style="background: var(--bg-page); color: var(--text-secondary); font-size: 0.75rem; border: 1px solid var(--border-color);">
                        Sem. ${i.semestreActual || 1}
                      </span>
                    </div>
                  </td>
                  <td>
                    <strong>${escapeHtml(i.programaNombre || 'Sin asignar')}</strong>
                  </td>
                  <td>
                    <button type="button" class="semillero-link-btn" data-view-semillero="${i.semilleroId}" data-integrante-id="${i.id}" title="Ver información del semillero">
                      <span>${escapeHtml(i.semilleroSigla || 'SEM')}</span>
                      <span class="text-muted" style="font-weight: 400;">· Ver info</span>
                    </button>
                  </td>
                  <td>
                    <div style="font-size: 0.82rem; line-height: 1.35;">
                      ${i.email ? `<div>✉️ ${escapeHtml(i.email)}</div>` : ''}
                      ${i.celular ? `<div>📱 ${escapeHtml(i.celular)}</div>` : ''}
                      ${
                        i.edad
                          ? `<div class="text-muted">🎂 <strong>${i.edad} años</strong> ${i.fechaNacimiento ? `(${i.fechaNacimiento})` : ''}</div>`
                          : ''
                      }
                    </div>
                  </td>
                  <td>
                    ${
                      i.esLider
                        ? '<span class="badge badge--leader">★ Líder</span>'
                        : '<span class="badge badge--member">Integrante</span>'
                    }
                  </td>
                  <td style="text-align: right;">
                    <div class="card-actions" style="justify-content: flex-end;">
                      <button type="button" class="btn btn--soft-red btn--sm" data-view-profile="${i.id}" title="Ver ficha completa">Ver ficha</button>
                      <button type="button" class="btn btn--secondary btn--sm" data-edit-integrante="${i.id}">Editar</button>
                      <button type="button" class="btn btn--danger btn--sm" data-delete-integrante="${i.id}">Eliminar</button>
                    </div>
                  </td>
                </tr>`
                )
                .join('')}
            </tbody>
          </table>
        </div>
      </div>`
    : renderEmptyState(
        'No se encontraron integrantes',
        integrantes.length
          ? 'Ajusta los filtros de búsqueda para ver otros resultados.'
          : 'Aún no hay integrantes registrados.',
        `<button type="button" class="btn btn--primary" id="emptyCreateIntegranteBtn">+ Agregar integrante</button>`
      );

  container.innerHTML = `
    <section>
      <div class="page-header">
        <div>
          <h1 class="page-title">Integrantes de Semilleros</h1>
          <p class="page-subtitle">Consulta el directorio completo, inspecciona la información de su semillero, programa académico o actualiza sus datos.</p>
        </div>
        <div class="page-actions">
          <button type="button" class="btn btn--primary" id="newIntegranteBtn">+ Nuevo Integrante</button>
        </div>
      </div>

      <div class="kpi-grid">
        <div class="kpi-card is-clickable ${!filters.soloLideres ? 'is-active' : ''}" id="kpiTotalIntegrantes" role="button" tabindex="0" title="Ver todos los integrantes">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px;">
            <span class="kpi-label">Cantidad de Integrantes</span>
            <span class="badge badge--member">Integrantes</span>
          </div>
          <span class="kpi-value">${totalIntegrantes}</span>
          <span class="kpi-hint">Estudiantes vinculados a semilleros</span>
        </div>

        <div class="kpi-card is-clickable ${filters.soloLideres ? 'is-active' : ''}" id="kpiTotalLideres" role="button" tabindex="0" title="Filtrar solo líderes">
          <div style="display: flex; align-items: center; justify-content: space-between; gap: 8px;">
            <span class="kpi-label">Cantidad de Líderes</span>
            <span class="badge badge--leader">★ Líderes</span>
          </div>
          <span class="kpi-value">${totalLideres}</span>
          <span class="kpi-hint">Integrantes con rol de liderazgo</span>
        </div>
      </div>

      <div class="toolbar">
        <div class="search-box">
          <span class="search-box__icon">🔍</span>
          <input type="search" class="search-box__input" id="intSearchInput"
            placeholder="Buscar por nombre, código, identificación, programa o semillero..."
            value="${escapeHtml(filters.query)}" />
        </div>
        <select class="filter-select" id="intSemilleroFilter" aria-label="Filtrar por semillero">
          <option value="">Todos los semilleros</option>
          ${semilleroOptions}
        </select>
        <select class="filter-select" id="intProgramaFilter" aria-label="Filtrar por programa académico">
          <option value="">Todos los programas</option>
          ${programaOptions}
        </select>
        <label class="checkbox-row" style="padding: 8px 12px;">
          <input type="checkbox" id="intLiderFilter" ${filters.soloLideres ? 'checked' : ''} />
          <span class="text-sm">Solo líderes</span>
        </label>
      </div>

      ${tableHtml}
    </section>
  `;

  const openNew = () => openIntegranteFormModal({}, () => renderIntegrantesView(container));
  container.querySelector('#newIntegranteBtn')?.addEventListener('click', openNew);
  container.querySelector('#emptyCreateIntegranteBtn')?.addEventListener('click', openNew);

  const toggleAll = () => {
    if (filters.soloLideres) {
      filters.soloLideres = false;
      renderIntegrantesView(container);
    }
  };

  const toggleLideres = () => {
    filters.soloLideres = !filters.soloLideres;
    renderIntegrantesView(container);
  };

  const kpiAll = container.querySelector('#kpiTotalIntegrantes');
  kpiAll?.addEventListener('click', toggleAll);
  kpiAll?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      toggleAll();
    }
  });

  const kpiLideres = container.querySelector('#kpiTotalLideres');
  kpiLideres?.addEventListener('click', toggleLideres);
  kpiLideres?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      toggleLideres();
    }
  });

  const searchInput = container.querySelector('#intSearchInput');
  searchInput?.addEventListener('input', (e) => {
    filters.query = e.target.value;
    renderIntegrantesView(container);
    const nextInput = container.querySelector('#intSearchInput');
    if (nextInput) {
      nextInput.focus();
      nextInput.setSelectionRange(nextInput.value.length, nextInput.value.length);
    }
  });

  container.querySelector('#intSemilleroFilter')?.addEventListener('change', (e) => {
    filters.semilleroId = e.target.value;
    renderIntegrantesView(container);
  });

  container.querySelector('#intProgramaFilter')?.addEventListener('change', (e) => {
    filters.programaId = e.target.value;
    renderIntegrantesView(container);
  });

  container.querySelector('#intLiderFilter')?.addEventListener('change', (e) => {
    filters.soloLideres = e.target.checked;
    renderIntegrantesView(container);
  });

  // Ver información del semillero desde el integrante
  container.querySelectorAll('[data-view-semillero]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const semillero = getSemilleroById(btn.dataset.viewSemillero);
      const integrante = store.integrantes.find(
        (item) => Number(item.id) === Number(btn.dataset.integranteId)
      );
      openSemilleroPreviewModal(semillero, integrante);
    });
  });

  // Ver ficha completa del integrante
  container.querySelectorAll('[data-view-profile]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const integrante = store.integrantes.find(
        (item) => Number(item.id) === Number(btn.dataset.viewProfile)
      );
      if (integrante) {
        openIntegranteDetailModal(integrante);
      }
    });
  });

  // Editar integrante
  container.querySelectorAll('[data-edit-integrante]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const integrante = store.integrantes.find(
        (item) => Number(item.id) === Number(btn.dataset.editIntegrante)
      );
      if (integrante) {
        openIntegranteFormModal({ integrante }, () => renderIntegrantesView(container));
      }
    });
  });

  // Eliminar integrante
  container.querySelectorAll('[data-delete-integrante]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const integrante = store.integrantes.find(
        (item) => Number(item.id) === Number(btn.dataset.deleteIntegrante)
      );
      if (!integrante) return;

      confirmDialog({
        title: 'Eliminar integrante',
        message: `¿Seguro que deseas eliminar a "${integrante.nombre}" (${integrante.codigo})?`,
        onConfirm: async () => {
          try {
            await integrantesApi.remove(integrante.id);
            await loadAllData();
            showToast('Integrante eliminado correctamente', 'success');
            renderIntegrantesView(container);
          } catch (err) {
            showToast(err.message, 'error');
            throw err;
          }
        },
      });
    });
  });
}
