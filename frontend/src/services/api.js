export async function getBrands() {
  try {
    const res = await fetch('/api/brands');
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (e) {
    console.error('getBrands error', e);
    return null;
  }
}

export async function getBrand(brandId) {
  try {
    const res = await fetch(`/api/brands/${brandId}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (e) {
    console.error('getBrand error', e);
    return null;
  }
}

export async function getBrandReviews(brandId) {
  try {
    const res = await fetch(`/api/brands/${brandId}/reviews`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (e) {
    console.error('getBrandReviews error', e);
    return [];
  }
}

export async function getBrandMenus(brandId) {
  try {
    const res = await fetch(`/api/brands/${brandId}/menus`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (e) {
    console.error('getBrandMenus error', e);
    return [];
  }
}

export async function getMenu(menuId) {
  try {
    const res = await fetch(`/api/menus/${menuId}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (e) {
    console.error('getMenu error', e);
    return null;
  }
}

export async function getMenuReviews(menuId) {
  try {
    const res = await fetch(`/api/menus/${menuId}/reviews`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (e) {
    console.error('getMenuReviews error', e);
    return [];
  }
}

export async function createMenuReview(menuId, payload) {
  try {
    const res = await fetch(`/api/menus/${menuId}/reviews`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (e) {
    console.error('createMenuReview error', e);
    throw e;
  }
}
