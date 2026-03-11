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
