import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';

export function Layout() {
  const { session, hasRole, logout } = useAuth();
  const navigate = useNavigate();
  const signOut = () => { logout(); navigate('/login'); };
  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><span className="brand-mark">S</span><span>Student<span>Hub</span></span></div>
      <div className="sidebar-label">Рабочее пространство</div>
      <nav className="nav">
        {hasRole('ADMIN', 'TEACHER') && <NavLink to="/students" className="nav-link"><span>♙</span>Студенты</NavLink>}
        {hasRole('SUPER_ADMIN') && <NavLink to="/users" className="nav-link"><span>♧</span>Пользователи</NavLink>}
      </nav>
      <div className="sidebar-bottom">
        <div className="user-card"><div className="avatar">{session?.email[0].toUpperCase()}</div><div><strong>{session?.email}</strong><small>{session?.roles.join(' · ')}</small></div></div>
        <button className="logout-button" onClick={signOut}>Выйти <span>↗</span></button>
      </div>
    </aside>
    <main className="main-content"><Outlet /></main>
  </div>;
}
