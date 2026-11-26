import { store, loadAllData, getSemillerosByGrupo } from '../state/store.js';
import { gruposInvestigacionApi } from '../api/gruposInvestigacionApi.js';
import { openModal, closeModal, confirmDialog } from '../ui/modal.js';
import { showToast } from '../ui/toast.js';
import { escapeHtml, renderEmptyState } from '../ui/components.js';

let searchQuery = '';
let facultyFilter = '';
let categoryFilter = '';

const UFPS_FACULTADES = [
  'Facultad de Ingeniería',
  'Facultad de Ciencias Empresariales',
  'Facultad de Ciencias Agrarias y del Ambiente',
  'Facultad de Educación, Artes y Humanidades',
  'Facultad de Ciencias Básicas',
  'Facultad de Ciencias de la Salud',
];

const MINCIENCIAS_CATEGORIAS = ['A1', 'A', 'B', 'C', 'Reconocido'];

function getCategoryBadge(categoria) {
  const cat = (categoria || '').toUpperCase();
  if (cat === 'A1') return '<span class="badge badge--success" style="font-weight: 700;">A1</span>';
  if (cat === 'A') return '<span class="badge badge--primary" style="font-weight: 600;">A</span>';
  if (cat === 'B') return '<span class="badge badge--warning" style="font-weight: 600;">B</span>';
  if (cat === 'C') return '<span class="badge badge--neutral" style="font-weight: 600;">C</span>';
  return `<span class="badge badge--neutral">${escapeHtml(categoria)}</span>`;
}

function openGrupoModal(grupo = null, onSaved) {
  const isEdit = Boolean(grupo);

  const facultadesOptions = UFPS_FACULTADES.map(
    (f) => `<option value="${f}" ${grupo?.facultad === f ? 'selected' : ''}>${f}</option>`
  ).join('');

  const categoriasOptions = MINCIENCIAS_CATEGORIAS.map(
    (c) => `<option value="${c}" ${grupo?.categoria === c ? 'selected' : ''}>${c}</option>`
  ).join('');

  openModal({
    title: isEdit ? `Editar Grupo · ${grupo.sigla}` : 'Nuevo Grupo de Investigación UFPS',
    size: 'md',
    bodyHtml: `
      <form id="grupoForm" class="form-grid">
        <div class="form-field form-field--full">
          <label class="form-label" for="grpNombre">Nombre oficial del grupo *</label>
          <input class="form-control" id="grpNombre" name="nombre" required maxlength="200"
            value="${escapeHtml(grupo?.nombre || '')}" placeholder="Ej. Grupo de Investigación y Desarrollo de Ingeniería del Software" />
        </div>

        <div class="form-field">
          <label class="form-label" for="grpSigla">Sigla *</label>
          <input class="form-control" id="grpSigla" name="sigla" required maxlength="30"
            value="${escapeHtml(grupo?.sigla || '')}" placeholder="Ej. GIDISOFT" />
        </div>

        <div class="form-field">
          <label class="form-label" for="grpCodigo">Código Minciencias *</label>
          <input class="form-control" id="grpCodigo" name="codigoMinciencias" required maxlength="30"
            value="${escapeHtml(grupo?.codigoMinciencias || '')}" placeholder="Ej. COL0006204" />
        </div>

        <div class="form-field">
          <label class="form-label" for="grpCategoria">Categoría Minciencias *</label>
          <select class="form-control" id="grpCategoria" name="categoria" required>
            <option value="">Seleccione categoría...</option>
            ${categoriasOptions}
          </select>
        </div>

        <div class="form-field">
          <label class="form-label" for="grpFacultad">Facultad UFPS *</label>
          <select class="form-control" id="grpFacultad" name="facultad" required>
            <option value="">Seleccione facultad...</option>
            ${facultadesOptions}
          </select>
        </div>

        <div class="form-field form-field--full">
          <label class="form-label" for="grpDepartamento">Departamento académico *</label>
          <input class="form-control" id="grpDepartamento" name="departamento" required maxlength="150"
            value="${escapeHtml(grupo?.departamento || '')}" placeholder="Ej. Sistemas e Informática" />
        </div>

        <div class="form-field">
          <label class="form-label" for="grpLider">Docente líder / Director *</label>
          <input class="form-control" id="grpLider" name="lider" required maxlength="180"
            value="${escapeHtml(grupo?.lider || '')}" placeholder="Ej. Dr. Carlos Eduardo Martínez" />
        </div>

        <div class="form-field">
          <label class="form-label" for="grpCorreo">Correo institucional *</label>
          <input class="form-control" id="grpCorreo" name="correo" type="email" required maxlength="180"
            value="${escapeHtml(grupo?.correo || '')}" placeholder="grupo@ufps.edu.co" />
        </div>

        <div class="form-field form-field--full">
          <label class="form-label" for="grpSitioWeb">Enlace GrupLAC / Sitio Web</label>
          <input class="form-control" id="grpSitioWeb" name="sitioWeb" maxlength="255"
            value="${escapeHtml(grupo?.sitioWeb || '')}" placeholder="https://scienti.minciencias.gov.co/gruplac/..." />
        </div>

        <div class="form-field form-field--full" id="grpFormError"></div>
      </form>
    `,
    footerHtml: `
      <button type="button" class="btn btn--secondary" data-modal-close>Cancelar</button>
      <button type="submit" form="grupoForm" class="btn btn--primary" id="saveGrpBtn">
        ${isEdit ? 'Guardar cambios' : 'Crear grupo'}
      </button>
    `,
    onMount: (backdrop) => {
      const form = backdrop.querySelector('#grupoForm');
      const errorBox = backdrop.querySelector('#grpFormError');
      const saveBtn = backdrop.querySelector('#saveGrpBtn');

      form.addEventListener('submit', async (e) => {
        e.preventDefault();
        errorBox.innerHTML = '';
        const fd = new FormData(form);
        const payload = {
          nombre: String(fd.get('nombre') || '').trim(),
          sigla: String(fd.get('sigla') || '').trim(),
          codigoMinciencias: String(fd.get('codigoMinciencias') || '').trim(),
          categoria: String(fd.get('categoria') || '').trim(),
          facultad: String(fd.get('facultad') || '').trim(),
          departamento: String(fd.get('departamento') || '').trim(),
          lider: String(fd.get('lider') || '').trim(),
          correo: String(fd.get('correo') || '').trim(),
          sitioWeb: String(fd.get('sitioWeb') || '').trim() || null,
        };

        saveBtn.disabled = true;
        try {
          if (isEdit) {
            await gruposInvestigacionApi.update(grupo.id, payload);
            showToast('Grupo de investigación actualizado', 'success');
          } else {
            await gruposInvestigacionApi.create(payload);
            showToast('Grupo de investigación registrado', 'success');
          }
          await loadAllData();
          closeModal();
          onSaved();
        } catch (err) {
          errorBox.innerHTML = `<div class="form-error">${escapeHtml(err.message)}</div>`;
          saveBtn.disabled = false;
        }
      });
    },
  });
}

export function renderGruposInvestigacionView(container) {
  const { gruposInvestigacion, semilleros } = store;
  const q = searchQuery.trim().toLowerCase();

  const filtered = gruposInvestigacion.filter((g) => {
    const matchesSearch =
      !q ||
      g.nombre.toLowerCase().includes(q) ||
      g.sigla.toLowerCase().includes(q) ||
      g.codigoMinciencias.toLowerCase().includes(q) ||
      g.facultad.toLowerCase().includes(q) ||
      g.departamento.toLowerCase().includes(q) ||
      g.lider.toLowerCase().includes(q) ||
      g.correo.toLowerCase().includes(q);

    const matchesFaculty = !facultyFilter || g.facultad === facultyFilter;
    const matchesCategory = !categoryFilter || g.categoria === categoryFilter;

    return matchesSearch && matchesFaculty && matchesCategory;
  });

  const facultyOptions = UFPS_FACULTADES.map(
    (f) => `<option value="${f}" ${facultyFilter === f ? 'selected' : ''}>${f}</option>`
  ).join('');

  const categoryOptions = MINCIENCIAS_CATEGORIAS.map(
    (c) => `<option value="${c}" ${categoryFilter === c ? 'selected' : ''}>Categoría ${c}</option>`
  ).join('');

  // Stats
  const catA1orA = gruposInvestigacion.filter((g) => ['A1', 'A'].includes((g.categoria || '').toUpperCase())).length;
  const facultiesSet = new Set(gruposInvestigacion.map((g) => g.facultad));

  const tableHtml = filtered.length
    ? `
      <div class="table-card">
        <div class="table-responsive">
          <table class="data-table">
            <thead>
              <tr>
                <th>Grupo de Investigación</th>
                <th>Cód. Minciencias</th>
                <th>Categoría</th>
                <th>Facultad & Dpto.</th>
                <th>Líder / Contacto</th>
                <th>Semilleros</th>
                <th style="text-align: right;">Acciones</th>
              </tr>
            </thead>
            <tbody>
              ${filtered
                .map((g) => {
                  const assignedSemilleros = getSemillerosByGrupo(g.id);
                  const semBadges = assignedSemilleros.length
                    ? assignedSemilleros
                        .map(
                          (s) =>
                            `<a href="#/semilleros/${s.id}" class="semillero-link-btn" style="margin-right: 4px;" title="${escapeHtml(s.nombre)}">${escapeHtml(s.sigla)}</a>`
                        )
                        .join('')
                    : '<span class="text-sm text-muted">Sin semilleros</span>';

                  return `
                  <tr>
                    <td>
                      <div>
                        <strong style="color: var(--text-color);">${escapeHtml(g.nombre)}</strong>
                        <div class="text-sm">
                          <span class="sigla-badge" style="font-size: 0.75rem;">${escapeHtml(g.sigla)}</span>
                          ${
                            g.sitioWeb
                              ? ` <a href="${escapeHtml(g.sitioWeb)}" target="_blank" rel="noopener noreferrer" class="text-sm" style="color: var(--primary-color);">GrupLAC ↗</a>`
                              : ''
                          }
                        </div>
                      </div>
                    </td>
                    <td>
                      <code style="font-size: 0.82rem; font-weight: 600;">${escapeHtml(g.codigoMinciencias)}</code>
                    </td>
                    <td>${getCategoryBadge(g.categoria)}</td>
                    <td>
                      <div><strong>${escapeHtml(g.facultad)}</strong></div>
                      <div class="text-sm text-muted">Dpto. ${escapeHtml(g.departamento)}</div>
                    </td>
                    <td>
                      <div><strong>${escapeHtml(g.lider)}</strong></div>
                      <div class="text-sm text-muted">${escapeHtml(g.correo)}</div>
                    </td>
                    <td>${semBadges}</td>
                    <td style="text-align: right;">
                      <div class="card-actions" style="justify-content: flex-end;">
                        <button type="button" class="btn btn--secondary btn--sm" data-edit-grupo="${g.id}">Editar</button>
                        <button type="button" class="btn btn--danger btn--sm" data-delete-grupo="${g.id}">Eliminar</button>
                      </div>
                    </td>
                  </tr>`;
                })
                .join('')}
            </tbody>
          </table>
        </div>
      </div>`
    : renderEmptyState(
        'No se encontraron grupos de investigación',
        gruposInvestigacion.length
          ? 'Prueba ajustando los filtros de búsqueda o categoría.'
          : 'Aún no hay grupos de investigación registrados.',
        `<button type="button" class="btn btn--primary" id="emptyNewGrupoBtn">+ Registrar primer grupo</button>`
      );

  container.innerHTML = `
    <section>
      <div class="page-header">
        <div>
          <h1 class="page-title">Grupos de Investigación UFPS</h1>
          <p class="page-subtitle">Catálogo institucional de grupos de investigación adscritos a la Vicerrectoría Asistente de Investigación y Extensión.</p>
        </div>
        <div class="page-actions">
          <button type="button" class="btn btn--primary" id="newGrupoBtn">+ Nuevo Grupo</button>
        </div>
      </div>

      <div class="kpi-grid">
        <div class="kpi-card">
          <span class="kpi-label">Total Grupos</span>
          <span class="kpi-value">${gruposInvestigacion.length}</span>
          <span class="kpi-hint">Avalados institucionalmente</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">Categoría A1 / A</span>
          <span class="kpi-value">${catA1orA}</span>
          <span class="kpi-hint">Máxima excelencia científica</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">Facultades UFPS</span>
          <span class="kpi-value">${facultiesSet.size}</span>
          <span class="kpi-hint">Unidades académicas activas</span>
        </div>
        <div class="kpi-card">
          <span class="kpi-label">Semilleros Vinculados</span>
          <span class="kpi-value">${semilleros.length}</span>
          <span class="kpi-hint">Investigación formativa</span>
        </div>
      </div>

      <div class="toolbar">
        <div class="search-box">
          <span class="search-box__icon">🔍</span>
          <input type="search" class="search-box__input" id="grupoSearchInput"
            placeholder="Buscar por nombre, sigla, código, facultad, líder o correo..."
            value="${escapeHtml(searchQuery)}" />
        </div>
        <select class="filter-select" id="grupoFacultyFilter" aria-label="Filtrar por facultad">
          <option value="">Todas las facultades</option>
          ${facultyOptions}
        </select>
        <select class="filter-select" id="grupoCategoryFilter" aria-label="Filtrar por categoría">
          <option value="">Todas las categorías</option>
          ${categoryOptions}
        </select>
      </div>

      ${tableHtml}
    </section>
  `;

  // Listeners
  container.querySelector('#newGrupoBtn')?.addEventListener('click', () => {
    openGrupoModal(null, () => renderGruposInvestigacionView(container));
  });

  container.querySelector('#emptyNewGrupoBtn')?.addEventListener('click', () => {
    openGrupoModal(null, () => renderGruposInvestigacionView(container));
  });

  const searchInput = container.querySelector('#grupoSearchInput');
  searchInput?.addEventListener('input', (e) => {
    searchQuery = e.target.value;
    renderGruposInvestigacionView(container);
    const nextInput = container.querySelector('#grupoSearchInput');
    if (nextInput) {
      nextInput.focus();
      nextInput.setSelectionRange(nextInput.value.length, nextInput.value.length);
    }
  });

  container.querySelector('#grupoFacultyFilter')?.addEventListener('change', (e) => {
    facultyFilter = e.target.value;
    renderGruposInvestigacionView(container);
  });

  container.querySelector('#grupoCategoryFilter')?.addEventListener('change', (e) => {
    categoryFilter = e.target.value;
    renderGruposInvestigacionView(container);
  });

  container.querySelectorAll('[data-edit-grupo]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const g = gruposInvestigacion.find((item) => Number(item.id) === Number(btn.dataset.editGrupo));
      if (g) openGrupoModal(g, () => renderGruposInvestigacionView(container));
    });
  });

  container.querySelectorAll('[data-delete-grupo]').forEach((btn) => {
    btn.addEventListener('click', () => {
      const g = gruposInvestigacion.find((item) => Number(item.id) === Number(btn.dataset.deleteGrupo));
      if (!g) return;

      const assigned = getSemillerosByGrupo(g.id);
      if (assigned.length > 0) {
        openModal({
          title: 'No se puede eliminar el grupo',
          size: 'sm',
          bodyHtml: `
            <p class="text-muted" style="line-height: 1.5;">
              El grupo <strong>${escapeHtml(g.sigla)}</strong> tiene <strong>${assigned.length}</strong> semillero(s) asociado(s) (<em>${assigned.map((s) => escapeHtml(s.sigla)).join(', ')}</em>).
            </p>
            <p class="text-sm text-muted" style="margin-top: 8px; line-height: 1.4;">
              Debes reasignar o eliminar los semilleros asociados antes de poder eliminar este grupo de investigación.
            </p>
          `,
          footerHtml: `<button type="button" class="btn btn--primary" data-modal-close>Entendido</button>`,
        });
        return;
      }

      confirmDialog({
        title: 'Eliminar Grupo de Investigación',
        message: `¿Estás seguro de que deseas eliminar el grupo "${g.sigla} · ${g.nombre}"? Esta acción no se puede deshacer.`,
        confirmLabel: 'Sí, eliminar',
        onConfirm: async () => {
          try {
            await gruposInvestigacionApi.remove(g.id);
            showToast('Grupo de investigación eliminado', 'success');
            await loadAllData();
            renderGruposInvestigacionView(container);
          } catch (err) {
            showToast(err.message, 'error');
          }
        },
      });
    });
  });
}
