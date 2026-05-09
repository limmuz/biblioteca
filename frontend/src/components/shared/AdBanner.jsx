import React, { useState } from 'react';
import styles from './AdBanner.module.css';

const ADS = [
  {
    id: 1,
    brand: 'Livraria Cultura',
    headline: 'Até 40% OFF em best-sellers',
    sub: 'Frete grátis acima de R$ 99',
    cta: 'Ver ofertas',
    accent: '#284239',
    bg: '#e8f4e8',
  },
  {
    id: 2,
    brand: 'Kindle Unlimited',
    headline: 'Leia mais de 3 milhões de títulos',
    sub: '30 dias grátis para novos assinantes',
    cta: 'Experimente',
    accent: '#2553a6',
    bg: '#e8f0fb',
  },
  {
    id: 3,
    brand: 'Clube do Livro',
    headline: 'Um livro novo todo mês',
    sub: 'Curadoria especializada, direto na sua porta',
    cta: 'Assinar agora',
    accent: '#8a5f00',
    bg: '#fef8e7',
  },
];

export default function AdBanner({ variant = 'sidebar' }) {
  const [dismissed, setDismissed] = useState(false);
  const ad = ADS[Math.floor(Math.random() * ADS.length)];

  if (dismissed) return null;

  return (
    <div
      className={variant === 'sidebar' ? styles.sidebar : styles.banner}
      style={{ '--ad-accent': ad.accent, '--ad-bg': ad.bg }}
    >
      <button className={styles.dismiss} onClick={() => setDismissed(true)} type="button" title="Fechar">✕</button>
      <span className={styles.label}>Patrocinado</span>
      <p className={styles.brand}>{ad.brand}</p>
      <p className={styles.headline}>{ad.headline}</p>
      <p className={styles.sub}>{ad.sub}</p>
      <button className={styles.cta} type="button">{ad.cta} →</button>
    </div>
  );
}
