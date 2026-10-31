import { useEffect, useState, type FormEvent } from 'react';
import { studentsApi } from '../api/services';
import { getApiError } from '../api/client';
import { useAuth } from '../auth';
import { EmptyState, Loading } from '../components/Loading';
import { Modal } from '../components/Modal';
import { useToast } from '../components/Toast';
import type { StudentFullResponse, StudentResponse } from '../types';

const emptyForm = { name: '', surname: '', phone_number: '', email: '' };
export function StudentsPage() {
  const { hasRole } = useAuth(); const canEdit = hasRole('ADMIN'); const notify = useToast();
  const [students, setStudents] = useState<StudentResponse[]>([]); const [loading, setLoading] = useState(true); const [loadError, setLoadError] = useState('');
  const [query, setQuery] = useState(''); const [selected, setSelected] = useState<StudentFullResponse | null>(null);
  const [modal, setModal] = useState<'create' | 'edit' | null>(null); const [form, setForm] = useState(emptyForm); const [pending, setPending] = useState(false);
  const load = async (signal?: AbortSignal) => { setLoading(true); setLoadError(''); try { const data = await studentsApi.list(signal); setStudents(data.response || []); } catch (err) { if ((err as { name?: string }).name !== 'CanceledError') setLoadError(getApiError(err)); } finally { if (!signal?.aborted) setLoading(false); } };
  useEffect(() => { const controller = new AbortController(); void load(controller.signal); return () => controller.abort(); }, []);
  const visible = students.filter((s) => `${s.name} ${s.surname}`.toLowerCase().includes(query.toLowerCase()));
  const openEdit = async (id: number) => {
    try {
      const student = await studentsApi.get(id);
      setSelected(student);
      setForm({ name: student.name, surname: student.surname, phone_number: student.phoneNumber || '', email: student.email || '' });
      setModal('edit');
    } catch (err) { notify(getApiError(err), 'error'); }
  };
  const submit = async (event: FormEvent) => { event.preventDefault(); if (!form.name || !form.surname || !form.email) return; setPending(true); try { if (modal === 'create') { await studentsApi.create(form); notify('Студент добавлен'); } else if (selected) { await studentsApi.update(selected.id, { email: form.email, phoneNumber: form.phone_number }); notify('Данные обновлены'); } setModal(null); await load(); } catch (err) { notify(getApiError(err), 'error'); } finally { setPending(false); } };
  const remove = async (student: StudentResponse) => { if (!window.confirm(`Удалить ${student.name} ${student.surname}?`)) return; try { await studentsApi.remove(student.id); setStudents((current) => current.filter((s) => s.id !== student.id)); notify('Студент удалён'); } catch (err) { notify(getApiError(err), 'error'); } };
  const showCreate = () => { setForm(emptyForm); setSelected(null); setModal('create'); };
  return <div className="page"><header className="page-header"><div><p className="eyebrow">Обзор</p><h1>Студенты</h1><p className="page-subtitle">Единый список студентов и контактной информации.</p></div>{canEdit && <button className="primary-button" onClick={showCreate}>+ Добавить студента</button>}</header>
    <section className="stats-row"><div className="stat-card"><span>Всего студентов</span><strong>{students.length}</strong><small>в базе данных</small></div><div className="stat-card accent"><span>Статус системы</span><strong>Онлайн</strong><small><i className="status-dot" /> API доступен</small></div></section>
    <section className="content-card"><div className="toolbar"><div><h2>Все студенты</h2><span className="muted">{students.length} записей</span></div><label className="search"><span>⌕</span><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Поиск по имени..." /></label></div>
      {loading ? <Loading /> : loadError ? <div className="error-state"><p>{loadError}</p><button className="secondary-button" onClick={() => void load()}>Повторить</button></div> : visible.length === 0 ? <EmptyState title={query ? 'Ничего не найдено' : 'Студентов пока нет'} text={query ? 'Измените поисковый запрос.' : 'Добавьте первую запись, чтобы начать.'} /> : <div className="table-wrap"><table><thead><tr><th>Студент</th><th>ID</th><th>Действия</th></tr></thead><tbody>{visible.map((student) => <tr key={student.id}><td><div className="person"><div className="person-avatar">{student.name[0]}{student.surname[0]}</div><div><strong>{student.name} {student.surname}</strong><small>Студент</small></div></div></td><td><span className="id-badge">#{student.id}</span></td><td><div className="row-actions"><button className="text-button" onClick={() => void openEdit(student.id)}>Подробнее</button>{canEdit && <><button className="icon-button" onClick={() => { setSelected({ ...student, phoneNumber: '', email: '' }); setForm({ name: student.name, surname: student.surname, phone_number: '', email: '' }); setModal('edit'); }} aria-label="Редактировать">✎</button><button className="icon-button danger" onClick={() => void remove(student)} aria-label="Удалить">⌫</button></>}</div></td></tr>)}</tbody></table></div>}
    </section>
    {modal && <Modal title={modal === 'create' ? 'Новый студент' : canEdit ? 'Редактировать студента' : 'Профиль студента'} onClose={() => setModal(null)}><form className="form-stack" onSubmit={submit}>{modal === 'create' && <><label>Имя<input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></label><label>Фамилия<input required value={form.surname} onChange={(e) => setForm({ ...form, surname: e.target.value })} /></label></>}<label>Email<input required={!(!canEdit && modal === 'edit')} readOnly={!canEdit && modal === 'edit'} type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></label><label>Телефон<input readOnly={!canEdit && modal === 'edit'} value={form.phone_number} onChange={(e) => setForm({ ...form, phone_number: e.target.value })} /></label>{(canEdit || modal === 'create') && <div className="modal-actions"><button type="button" className="secondary-button" onClick={() => setModal(null)}>Отмена</button><button className="primary-button" disabled={pending}>{pending ? 'Сохраняем...' : 'Сохранить'}</button></div>}</form></Modal>}
  </div>;
}
