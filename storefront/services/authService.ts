const TOKEN_KEY = 'access_token';

let sessionAuthenticated = false;

export function isAuthenticated(): boolean {
  if (typeof window === 'undefined') {
    return false;
  }
  const token = getToken();
  return (token !== null && token !== '') || sessionAuthenticated;
}

export function getToken(): string | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return localStorage.getItem(TOKEN_KEY);
}

export function redirectToLogin(returnUrl: string): void {
  if (typeof window !== 'undefined') {
    window.location.href = `/login?returnUrl=${encodeURIComponent(returnUrl)}`;
  }
}

/**
 * Storefront sign-in is an OAuth2 session on the BFF, not a browser JWT.
 * Confirm that session (or a stored access token) before treating the shopper as signed in.
 */
export async function refreshAuthentication(): Promise<boolean> {
  if (typeof window === 'undefined') {
    return false;
  }
  const token = getToken();
  if (token) {
    sessionAuthenticated = true;
    return true;
  }
  try {
    const response = await fetch('/authentication');
    if (!response.ok) {
      sessionAuthenticated = false;
      return false;
    }
    const data = (await response.json()) as { isAuthenticated?: boolean };
    sessionAuthenticated = Boolean(data?.isAuthenticated);
    return sessionAuthenticated;
  } catch {
    sessionAuthenticated = false;
    return false;
  }
}
