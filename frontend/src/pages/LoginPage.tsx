import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';
import { getApiError } from '../api/client';
import { useToast } from '../components/Toast';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const notify = useToast();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [pending, setPending] = useState(false);
  const [error, setError] = useState('');
  const submit = async (event: FormEvent) => {
    event.preventDefault(); setError('');
    if (!email || !password) { setError('Введите email и пароль.'); return; }
    setPending(true);
    try { await login({ email, password }); notify('Добро пожаловать!'); navigate('/'); }
    catch (err) { const message = getApiError(err); setError(message); notify(message, 'error'); }
    finally { setPending(false); }
  };
  return <div className="login-page"><div className="login-panel">
    <div className="login-brand"><span className="brand-mark">S</span><span>Student<span>Hub</span></span></div>
    <div className="login-intro"><p className="eyebrow">Рабочее пространство</p><h1>С возвращением</h1><p>Войдите, чтобы управлять данными студентов.</p></div>
    <form onSubmit={submit} className="form-stack">
      <label>Email<input type="email" autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@company.com" /></label>
      <label>Пароль<input type="password" autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Введите пароль" /></label>
      {error && <div className="form-error">{error}</div>}
      <button className="primary-button wide" disabled={pending}>{pending ? 'Проверяем...' : 'Войти'} <span>→</span></button>
    </form>
    <div className="login-footer">JWT-аутентификация · безопасное соединение</div>
  </div><div className="login-art"><div className="art-orbit orbit-one" /><div className="art-orbit orbit-two" /><div className="art-copy"><span>01</span><h2>Порядок<br />в данных.</h2><p>Управляйте учебной информацией просто и уверенно.</p></div></div></div>;
}
