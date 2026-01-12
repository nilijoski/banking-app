import { API_URL } from '../config';
import type {User, Transaction, SavedRecipient} from '../types';

export const fetchUser = async (accountNumber: string): Promise<User> => {
    const response = await fetch(`${API_URL}/users/number/${accountNumber}`);
    if (!response.ok) throw new Error('Failed to fetch account');
    return response.json();
};

export const fetchTransactions = async (iban: string): Promise<Transaction[]> => {
    const response = await fetch(`${API_URL}/transactions/iban/${iban}`);
    if (!response.ok) throw new Error('Failed to fetch transactions');
    return response.json();
};

export const fetchSavedRecipients = async (userId: string): Promise<SavedRecipient[]> => {
    const response = await fetch(`${API_URL}/users/${userId}/saved-recipients`);
    if (!response.ok) throw new Error('Failed to fetch saved recipients');
    return response.json();
};

export const createTransfer = async (transferData: {
    fromIban: string;
    toIban: string;
    toFirstName: string;
    toLastName: string;
    amount: number;
    description: string;
}) => {
    const response = await fetch(`${API_URL}/transactions/transfer`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(transferData),
    });

    const result = await response.json();
    if (!result.success) {
        throw new Error(result.message || 'Transfer failed');
    }
    return result;
};

export const saveRecipient = async (userId: string, recipientIban: string) => {
    const response = await fetch(`${API_URL}/users/${userId}/saved-recipients`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ recipientIban }),
    });

    if (!response.ok) throw new Error('Failed to save recipient');
    return response.json();
};

export const deleteRecipient = async (userId: string, recipientIban: string) => {
    const response = await fetch(`${API_URL}/users/${userId}/saved-recipients/${recipientIban}`, {
        method: 'DELETE',
    });

    if (!response.ok) throw new Error('Failed to delete recipient');
};

export const deleteUser = async (userId: string) => {
    const response = await fetch(`${API_URL}/users/${userId}`, {
        method: 'DELETE',
    });

    if (!response.ok) throw new Error('Failed to delete recipient');
};