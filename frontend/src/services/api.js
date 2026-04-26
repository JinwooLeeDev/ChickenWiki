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
    const res = await fetch(`/api/menus/${menuId}/reviews`, {
      headers: getAuthHeaders(),
    });
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (e) {
    console.error('getMenuReviews error', e);
    return [];
  }
}

function getStoredUser() {
  try {
    const raw = localStorage.getItem('chickenwikiUser');
    return raw ? JSON.parse(raw) : null;
  } catch (e) {
    console.error('getStoredUser error', e);
    return null;
  }
}

async function parseError(res, fallbackMessage) {
  try {
    const data = await res.json();
    if (data?.message) return data.message;
  } catch (e) {
    console.error('parseError error', e);
  }

  return fallbackMessage;
}

function getAuthHeaders() {
  const currentUser = getStoredUser();

  return {
    'Content-Type': 'application/json',
    ...(currentUser?.token ? { Authorization: `Bearer ${currentUser.token}` } : {}),
  };
}

export async function createMenuReview(menuId, payload) {
  try {
    const res = await fetch(`/api/menus/${menuId}/reviews`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      throw new Error(await parseError(res, '리뷰 등록에 실패했습니다.'));
    }

    return await res.json();
  } catch (e) {
    console.error('createMenuReview error', e);
    throw e;
  }
}

export async function updateMenuReview(menuId, reviewId, payload) {
  try {
    const res = await fetch(`/api/menus/${menuId}/reviews/${reviewId}`, {
      method: 'PUT',
      headers: getAuthHeaders(),
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      throw new Error(await parseError(res, '리뷰 수정에 실패했습니다.'));
    }

    return await res.json();
  } catch (e) {
    console.error('updateMenuReview error', e);
    throw e;
  }
}

export async function deleteMenuReview(menuId, reviewId) {
  try {
    const res = await fetch(`/api/menus/${menuId}/reviews/${reviewId}`, {
      method: 'DELETE',
      headers: getAuthHeaders(),
    });

    if (!res.ok) {
      throw new Error(await parseError(res, '리뷰 삭제에 실패했습니다.'));
    }
  } catch (e) {
    console.error('deleteMenuReview error', e);
    throw e;
  }
}

export async function recommendMenuReview(menuId, reviewId) {
  try {
    const res = await fetch(`/api/menus/${menuId}/reviews/${reviewId}/recommend`, {
      method: 'POST',
      headers: getAuthHeaders(),
    });

    if (!res.ok) {
      throw new Error(await parseError(res, '리뷰 추천에 실패했습니다.'));
    }

    return await res.json();
  } catch (e) {
    console.error('recommendMenuReview error', e);
    throw e;
  }
}

export async function signup(payload) {
  const res = await fetch('/api/users/signup', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    throw new Error(await parseError(res, '회원가입에 실패했습니다.'));
  }

  return await res.json();
}

export async function login(payload) {
  const res = await fetch('/api/users/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    throw new Error(await parseError(res, '로그인에 실패했습니다.'));
  }

  return await res.json();
}

export async function getMyPage() {
  const res = await fetch('/api/users/me', {
    headers: getAuthHeaders(),
  });

  if (!res.ok) {
    throw new Error(await parseError(res, '마이페이지 정보를 불러오지 못했습니다.'));
  }

  return await res.json();
}

export async function getAdminUserByNickname(nickname) {
  const res = await fetch(`/api/users/admin/by-nickname/${encodeURIComponent(nickname)}`, {
    headers: getAuthHeaders(),
  });

  if (!res.ok) {
    throw new Error(await parseError(res, '사용자 정보를 불러오지 못했습니다.'));
  }

  return await res.json();
}

export async function deleteAdminUserByNickname(nickname) {
  const res = await fetch(`/api/users/admin/by-nickname/${encodeURIComponent(nickname)}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!res.ok) {
    throw new Error(await parseError(res, '계정을 삭제하지 못했습니다.'));
  }
}
