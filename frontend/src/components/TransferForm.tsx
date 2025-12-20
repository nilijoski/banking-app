import { useState, type FormEvent } from 'react';
import type {SavedRecipient} from '../types';
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
    const [showSaveRecipient, setShowSaveRecipient] = useState(false);

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

    const handleRecipientSelect = (recipientId: string) => {
        setSelectedRecipient(recipientId);
        if (recipientId) {
            const recipient = savedRecipients.find(r => r.id === recipientId);
            if (recipient) {
                setManualIban(recipient.iban);
                setFirstName(recipient.firstName);
                setLastName(recipient.lastName);
            }
        } else {
            setManualIban('');
            setFirstName('');
            setLastName('');
        }
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
        setShowSaveRecipient(false);
    };

    const handleSave = async () => {
        await onSaveRecipient(manualIban, firstName, lastName);
        setShowSaveRecipient(false);
    };

    return (
        <div className="transfer-section">
            <form onSubmit={handleSubmit}>
                {savedRecipients.length > 0 && (
                    <div className="contact-selector">
                        <select
                            value={selectedRecipient}
                            onChange={(e) => handleRecipientSelect(e.target.value)}
                            disabled={loading}
                        >
                            <option value="">-- Select Saved Recipient --</option>
                            {savedRecipients.map((r) => (
                                <option key={r.id} value={r.id}>
                                    {r.firstName} {r.lastName} - {formatIban(r.iban)}
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                <div className="form-group">
                    <label htmlFor="recipientIban">Recipient IBAN</label>
                    <input
                        id="recipientIban"
                        type="text"
                        value={manualIban}
                        onChange={(e) => handleIbanChange(e.target.value)}
                        required
                        disabled={loading}
                        placeholder="DE89 3704 0044 0532 0130 00"
                        className={ibanError ? 'input-error' : ''}
                    />
                    {ibanError && <div className="field-error">{ibanError}</div>}
                </div>

                <div className="form-group">
                    <label htmlFor="recipientFirstName">Recipient First Name</label>
                    <input
                        id="recipientFirstName"
                        type="text"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                        required
                        disabled={loading}
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="recipientLastName">Recipient Last Name</label>
                    <input
                        id="recipientLastName"
                        type="text"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                        required
                        disabled={loading}
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="amount">Amount (€)</label>
                    <input
                        id="amount"
                        type="text"
                        inputMode="decimal"
                        value={amount}
                        onChange={(e) => {
                            const v = e.target.value;
                            if (v === '' || /^\d*\.?\d{0,2}$/.test(v)) setAmount(v);
                        }}
                        required
                        disabled={loading}
                        placeholder="0.00"
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="description">Description (optional)</label>
                    <input
                        id="description"
                        type="text"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        disabled={loading}
                    />
                </div>

                <button type="submit" disabled={loading}>
                    {loading ? 'Processing...' : 'Send Money'}
                </button>

                {!showSaveRecipient && manualIban && firstName && lastName && (
                    <button
                        type="button"
                        onClick={() => setShowSaveRecipient(true)}
                        style={{ marginTop: '10px', background: '#28a745' }}
                        disabled={loading}
                    >
                        Save as Recipient
                    </button>
                )}

                {showSaveRecipient && (
                    <div className="save-contact-section">
                        <h4>Save Recipient</h4>
                        <button type="button" onClick={handleSave} style={{ background: '#28a745' }}>
                            Save Recipient
                        </button>
                        <button
                            type="button"
                            onClick={() => setShowSaveRecipient(false)}
                            style={{ marginLeft: '10px', background: '#6c757d' }}
                        >
                            Cancel
                        </button>
                    </div>
                )}
            </form>
        </div>
    );
}

export default TransferForm;