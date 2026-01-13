import { useState, type FormEvent } from 'react';
import type {SavedRecipient} from '../types/types.ts';
import { validateIban, formatIban } from '../utils/formatters';

interface TransferFormProps {
    savedRecipients: SavedRecipient[];
    loading: boolean;
    onSubmit: (data: {
        iban: string;
        firstName: string;
        lastName: string;
        amount: string;
        description: string;
    }) => Promise<void>;
    onSaveRecipient: (iban: string, firstName: string, lastName: string) => Promise<void>;
}

function TransferForm({ savedRecipients, loading, onSubmit, onSaveRecipient }: Readonly<TransferFormProps>) {
    const [selectedRecipient, setSelectedRecipient] = useState('');
    const [manualIban, setManualIban] = useState('');
    const [ibanError, setIbanError] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [amount, setAmount] = useState('');
    const [description, setDescription] = useState('');

    const handleIbanChange = (value: string) => {
        setManualIban(value);
        if (value.length > 0) {
            if (validateIban(value)) {
                setIbanError('');
            } else {
                setIbanError('Invalid IBAN format');
            }
        } else {
            setIbanError('');
        }
    };

    const handleRecipientSelect = (recipient: SavedRecipient) => {
        setSelectedRecipient(recipient.id);
        setManualIban(recipient.iban);
        setFirstName(recipient.firstName);
        setLastName(recipient.lastName);
    };

    const clearRecipient = () => {
        setSelectedRecipient('');
        setManualIban('');
        setFirstName('');
        setLastName('');
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        await onSubmit({ iban: manualIban, firstName, lastName, amount, description });

        // Reset form
        setAmount('');
        setDescription('');
        setSelectedRecipient('');
        setManualIban('');
        setFirstName('');
        setLastName('');
    };

    const handleSave = async () => {
        await onSaveRecipient(manualIban, firstName, lastName);
    };

    return (
        <div className="transfer-section-v2">
            {savedRecipients.length > 0 && (
                <div className="saved-recipients-compact">
                    <label className="compact-label">Saved Recipients</label>
                    <select
                        className="compact-select"
                        value={selectedRecipient}
                        onChange={(e) => {
                            const recipientId = e.target.value;
                            if (recipientId) {
                                const recipient = savedRecipients.find(r => r.id === recipientId);
                                if (recipient) handleRecipientSelect(recipient);
                            } else {
                                clearRecipient();
                            }
                        }}
                        disabled={loading}
                    >
                        <option value="">New recipient or select...</option>
                        {savedRecipients.map((recipient) => (
                            <option key={recipient.id} value={recipient.id}>
                                {recipient.firstName} {recipient.lastName} • {formatIban(recipient.iban)}
                            </option>
                        ))}
                    </select>
                </div>
            )}

            <form onSubmit={handleSubmit} className="transfer-form-v2">
                <div className="form-compact">
                    <div className="input-row">
                        <div className="input-col">
                            <input
                                type="text"
                                value={firstName}
                                onChange={(e) => {
                                    setFirstName(e.target.value);
                                    setSelectedRecipient('');
                                }}
                                required
                                disabled={loading}
                                placeholder="First Name"
                                className="compact-input"
                            />
                        </div>
                        <div className="input-col">
                            <input
                                type="text"
                                value={lastName}
                                onChange={(e) => {
                                    setLastName(e.target.value);
                                    setSelectedRecipient('');
                                }}
                                required
                                disabled={loading}
                                placeholder="Last Name"
                                className="compact-input"
                            />
                        </div>
                    </div>

                    <div className="input-full">
                        <input
                            type="text"
                            value={manualIban}
                            onChange={(e) => {
                                handleIbanChange(e.target.value);
                                setSelectedRecipient('');
                            }}
                            required
                            disabled={loading}
                            placeholder="IBAN"
                            className={`compact-input ${ibanError ? 'error' : ''}`}
                        />
                        <div className="error-text">{ibanError || '\u00A0'}</div>
                    </div>

                    <div className="input-row">
                        <div className="input-col">
                            <div className="amount-wrapper">
                                <span className="euro-sign">€</span>
                                <input
                                    type="text"
                                    inputMode="decimal"
                                    value={amount}
                                    onChange={(e) => {
                                        const v = e.target.value;
                                        if (v === '' || /^\d*\.?\d{0,2}$/.test(v)) setAmount(v);
                                    }}
                                    required
                                    disabled={loading}
                                    placeholder="Amount"
                                    className="compact-input amount-input"
                                />
                            </div>
                        </div>
                        <div className="input-col">
                            <input
                                type="text"
                                value={description}
                                onChange={(e) => setDescription(e.target.value)}
                                disabled={loading}
                                placeholder="Description (optional)"
                                className="compact-input"
                            />
                        </div>
                    </div>
                </div>

                <div className="action-bar">
                    {manualIban && firstName && lastName && !selectedRecipient && !ibanError && validateIban(manualIban) && !savedRecipients.some(r => r.iban.replaceAll(/\s/g, '') === manualIban.replaceAll(/\s/g, '')) && (
                        <button
                            type="button"
                            className="save-link"
                            onClick={handleSave}
                            disabled={loading}
                        >
                            + Save Recipient
                        </button>
                    )}
                    <button type="submit" className="send-btn" disabled={loading}>
                        {loading ? 'Sending...' : 'Send Money'}
                    </button>
                </div>
            </form>
        </div>
    );
}

export default TransferForm;