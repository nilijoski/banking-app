import { useEffect, useState } from 'react';
import type { User, Transaction, SavedRecipient } from '../types/types.ts';
import { useInactivityTimer } from '../hooks/useInactivityTimer';
import { formatIban, formatTime, validateIban } from '../utils/formatters';
import * as api from '../services/api';
import TransactionsList from './TransactionsList';
import TransferForm from './TransferForm';
import SavedRecipientsList from './SavedRecipientsList';

interface DashboardProps {
  user: User;
  onLogout: () => void;
}

function Dashboard({ user, onLogout }: Readonly<DashboardProps>) {
  const [userState, setUserState] = useState<User>(user);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [savedRecipients, setSavedRecipients] = useState<SavedRecipient[]>([]);
  const [activeTab, setActiveTab] =
      useState<'transactions' | 'sendMoney' | 'savedRecipients' | 'settings'>('transactions');
  const [message, setMessage] = useState('');
  const [warning, setWarning] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const timeLeft = useInactivityTimer(onLogout, 300000);

  const loadData = async () => {
    try {
      const [userData, transactionsData, recipientsData] = await Promise.all([
        api.fetchUser(user.accountNumber),
        api.fetchTransactions(user.iban),
        api.fetchSavedRecipients(user.id),
      ]);

      setUserState(userData);
      setTransactions(transactionsData);
      setSavedRecipients(recipientsData);
    } catch (err) {
      console.error('Failed to load data', err);
    }
  };

  useEffect(() => {
    void loadData()
    const refreshInterval = setInterval(loadData, 10000);
    return () => clearInterval(refreshInterval);
  }, [user.accountNumber, user.iban, user.id]);

  useEffect(() => {
    if (error || message || warning) {
      const timer = setTimeout(() => {
        setError('');
        setMessage('');
        setWarning('');
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [error, message, warning]);

  const handleTransfer = async (data: {
    iban: string;
    firstName: string;
    lastName: string;
    amount: string;
    description: string;
  }) => {
    setError('');
    setMessage('');
    setWarning('');
    setLoading(true);

    if (!data.iban || !validateIban(data.iban) || !data.firstName || !data.lastName) {
      setError('Please fill in all required fields');
      setLoading(false);
      throw new Error('Invalid form data');
    }

    try {
      const result = await api.createTransfer({
        fromIban: userState.iban,
        toIban: data.iban.replaceAll(/\s/g, ''),
        toFirstName: data.firstName,
        toLastName: data.lastName,
        amount: Number.parseFloat(data.amount),
        description: data.description || 'Transfer',
      });

      if (result.transaction?.warning) setWarning(result.transaction.warning);

      setMessage('Transfer successful!');
      await loadData();
      setTimeout(() => setActiveTab('transactions'), 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Transfer failed');
      throw err;
    } finally {
      setLoading(false);
    }
  };

  const handleSaveRecipient = async (iban: string, firstName: string, lastName: string) => {
    if (!iban || !firstName || !lastName) {
      setError('Please fill in all recipient details');
      throw new Error('Invalid recipient data');
    }

    try {
      await api.saveRecipient(userState.id, iban.replaceAll(/\s/g, ''), firstName, lastName);
      setMessage('Recipient saved successfully!');
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save recipient');
      throw err;
    }
  };

  const handleDeleteRecipient = async (recipientIban: string) => {
    try {
      await api.deleteRecipient(userState.id, recipientIban);
      setMessage('Recipient deleted');
      await loadData();
    } catch (err) {
      console.error('Failed to delete recipient', err);
      setError('Failed to delete recipient');
    }
  };

  const handleDeleteAccount = async () => {
    if (!globalThis.confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      return;
    }

    setLoading(true);
    try {
      await api.deleteUser(userState.id);
      setMessage('Account deleted successfully');
      setTimeout(() => onLogout(), 2000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete account');
    } finally {
      setLoading(false);
    }
  };

  return (
      <div className="dashboard">
        <div className="dashboard-header">
          <h2>
            Welcome, {userState.firstName} {userState.lastName}!
          </h2>
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
          <span style={{ fontSize: '14px', color: timeLeft < 60 ? '#dc3545' : '#666' }}>
            Auto-logout: {formatTime(timeLeft)}
          </span>
            <button onClick={onLogout} className="logout-btn">
              Logout
            </button>
          </div>
        </div>

        <div className="balance-section">
          <div className="balance-info">
            <h3>{formatIban(userState.iban)}</h3>
            <div className="balance-amount">€{userState.balance.toFixed(2)}</div>
          </div>
          <button className="send-money-btn" onClick={() => setActiveTab('sendMoney')}>
            Send Money
          </button>
        </div>

        <div className="nav-tabs">
          <button
              className={`nav-tab ${activeTab === 'transactions' ? 'active' : ''}`}
              onClick={() => setActiveTab('transactions')}
          >
            Transactions
          </button>
          <button
              className={`nav-tab ${activeTab === 'sendMoney' ? 'active' : ''}`}
              onClick={() => setActiveTab('sendMoney')}
          >
            Send Money
          </button>
          <button
              className={`nav-tab ${activeTab === 'savedRecipients' ? 'active' : ''}`}
              onClick={() => setActiveTab('savedRecipients')}
          >
            Saved Recipients
          </button>
          <button
              className={`nav-tab ${activeTab === 'settings' ? 'active' : ''}`}
              onClick={() => setActiveTab('settings')}
          >
            Settings
          </button>
        </div>

        <div className="dashboard-content">
          {error && <div className="error">{error}</div>}
          {warning && <div className="warning">{warning}</div>}
          {message && <div className="success">{message}</div>}

          {activeTab === 'transactions' && (
              <TransactionsList transactions={transactions} userIban={userState.iban} />
          )}

          {activeTab === 'sendMoney' && (
              <TransferForm
                  savedRecipients={savedRecipients}
                  loading={loading}
                  onSubmit={handleTransfer}
                  onSaveRecipient={handleSaveRecipient}
              />
          )}

          {activeTab === 'savedRecipients' && (
              <SavedRecipientsList recipients={savedRecipients} onDelete={handleDeleteRecipient} />
          )}

          {activeTab === 'settings' && (
              <div className="settings">
                <h3>Account Settings</h3>
                <div className="settings-content">
                  <div className="account-info">
                    <p><strong>Account Number:</strong> {userState.accountNumber}</p>
                    <p><strong>IBAN:</strong> {formatIban(userState.iban)}</p>
                    <p><strong>Name:</strong> {userState.firstName} {userState.lastName}</p>
                  </div>
                  <div className="danger-zone">
                    <h4>Danger Zone</h4>
                    <button
                        className="delete-account-btn"
                        onClick={handleDeleteAccount}
                        disabled={loading}
                    >
                      {loading ? 'Deleting...' : 'Delete account permanently'}
                    </button>
                    <p className="warning-text">Once you delete your account, there is no going back. Please be certain.</p>
                  </div>
                </div>
              </div>
          )}
        </div>
      </div>
  );
}

export default Dashboard;