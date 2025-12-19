import { useEffect, useState } from 'react';
import type { User, Transaction, SavedRecipient } from '../types';
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
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [savedRecipients, setSavedRecipients] = useState<SavedRecipient[]>([]);
  const [activeTab, setActiveTab] =
      useState<'transactions' | 'sendMoney' | 'savedRecipients'>('transactions');
  const [message, setMessage] = useState('');
  const [warning, setWarning] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const timeLeft = useInactivityTimer(onLogout, 300000);

  const loadData = async () => {
    try {
      const [, transactionsData, recipientsData] = await Promise.all([
        api.fetchUser(user.accountNumber),
        api.fetchTransactions(user.iban),
        api.fetchSavedRecipients(user.id),
      ]);

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
  }, [user]);

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
        fromIban: user.iban,
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
      await api.saveRecipient(user.id, iban.replaceAll(/\s/g, ''));
      setMessage('Recipient saved successfully!');
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save recipient');
      throw err;
    }
  };

  const handleDeleteRecipient = async (recipientIban: string) => {
    try {
      await api.deleteRecipient(user.id, recipientIban);
      await loadData();
    } catch (err) {
      console.error('Failed to delete recipient', err);
    }
  };

  return (
      <div className="dashboard">
        <div className="dashboard-header">
          <h2>
            Welcome, {user.firstName} {user.lastName}!
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
            <h3>{formatIban(user.iban)}</h3>
            <div className="balance-amount">€{user.balance.toFixed(2)}</div>
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
        </div>

        {error && <div className="error">{error}</div>}
        {warning && <div className="warning">{warning}</div>}
        {message && <div className="success">{message}</div>}

        {activeTab === 'transactions' && (
            <TransactionsList transactions={transactions} userIban={user.iban} />
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
      </div>
  );
}

export default Dashboard;
