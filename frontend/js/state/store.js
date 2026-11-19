import { semillerosApi } from '../api/semillerosApi.js';
import { integrantesApi } from '../api/integrantesApi.js';
import { lineasApi } from '../api/lineasApi.js';
import { directoresApi } from '../api/directoresApi.js';
import { productosApi } from '../api/productosApi.js';
import { tiposActividadApi } from '../api/tiposActividadApi.js';
import { actividadesApi } from '../api/actividadesApi.js';
import { capacitacionesApi } from '../api/capacitacionesApi.js';
import { gruposInvestigacionApi } from '../api/gruposInvestigacionApi.js';

const listeners = new Set();

export const store = {
  semilleros: [],
  integrantes: [],
  lineas: [],
  directores: [],
  gruposInvestigacion: [],
  productos: [],
  tiposProducto: [],
  tiposActividad: [],
  actividades: [],
  capacitaciones: [],
  apiOnline: false,
  lastError: null,
};

export function subscribe(listener) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

function notify() {
  listeners.forEach((fn) => fn(store));
}

export async function loadAllData() {
  try {
    const [semilleros, integrantes, lineas, directores, gruposInvestigacion, productos, tiposProducto, tiposActividad, actividades, capacitaciones] = await Promise.all([
      semillerosApi.getAll(),
      integrantesApi.getAll(),
      lineasApi.getAll(),
      directoresApi.getAll(),
      gruposInvestigacionApi.getAll(),
      productosApi.getAll(),
      productosApi.getTipos(),
      tiposActividadApi.getAll(),
      actividadesApi.getAll(),
      capacitacionesApi.getAll(),
    ]);

    store.semilleros = semilleros;
    store.integrantes = integrantes;
    store.lineas = lineas;
    store.directores = directores;
    store.gruposInvestigacion = gruposInvestigacion;
    store.productos = productos;
    store.tiposProducto = tiposProducto;
    store.tiposActividad = tiposActividad;
    store.actividades = actividades;
    store.capacitaciones = capacitaciones;
    store.apiOnline = true;
    store.lastError = null;
  } catch (error) {
    store.apiOnline = false;
    store.lastError = error.message;
    throw error;
  } finally {
    notify();
  }
}

export function getGrupoInvestigacionById(id) {
  return store.gruposInvestigacion.find((g) => Number(g.id) === Number(id)) || null;
}

export function getSemillerosByGrupo(grupoId) {
  return store.semilleros.filter((s) => Number(s.grupoInvestigacion?.id) === Number(grupoId));
}

export function getSemilleroById(id) {
  return store.semilleros.find((s) => Number(s.id) === Number(id)) || null;
}

export function getIntegrantesBySemillero(semilleroId) {
  return store.integrantes.filter((i) => Number(i.semilleroId) === Number(semilleroId));
}

export function getProductosBySemillero(semilleroId) {
  return store.productos.filter((p) => Number(p.semilleroId) === Number(semilleroId));
}

export function getActividadesBySemillero(semilleroId) {
  return store.actividades.filter((a) => Number(a.semilleroId) === Number(semilleroId));
}

export function getCapacitacionesBySemillero(semilleroId) {
  return store.capacitaciones.filter((c) => Number(c.semilleroId) === Number(semilleroId));
}
