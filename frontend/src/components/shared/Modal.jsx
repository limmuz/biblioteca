import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import styles from './Modal.module.css';

export default function Modal({
  title,
  message,
  onConfirm,
  onCancel,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  danger = false,
  singleButton = false,
}) {
  useEffect(() => {
    const handleKey = (e) => { if (e.key === 'Escape' && onCancel) onCancel(); };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [onCancel]);

  return (
    <div className={styles.overlay} onClick={onCancel}>
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        {title && <h2 className={styles.title}>{title}</h2>}
        {message && <p className={styles.message}>{message}</p>}
        <div className={styles.actions}>
          {!singleButton && onCancel && (
            <button className={styles.btnSecondary} onClick={onCancel} type="button">
              {cancelLabel}
            </button>
          )}
          <button
            className={danger ? styles.btnDanger : styles.btnPrimary}
            onClick={onConfirm}
            type="button"
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

Modal.propTypes = {
  title: PropTypes.string,
  message: PropTypes.string,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func,
  confirmLabel: PropTypes.string,
  cancelLabel: PropTypes.string,
  danger: PropTypes.bool,
  singleButton: PropTypes.bool,
};
