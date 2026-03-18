import styles from './Footer.module.css';
import LogoIcon from '../../assets/icon-logo.svg?react';

export default function Footer() {
  return (
    <footer className={styles.footer} role="contentinfo">
      <div className={styles.logo}>
        <span className={styles.logoIconWrap}>
          <LogoIcon className={styles.logoIcon} aria-hidden="true" />
        </span>
        <span className={styles.brandName}>Lybre</span>
      </div>

      <p className={styles.tagline}>Histórias sem limites, onde a leitura é livre!</p>

      <hr className={styles.divider} />

      <p className={styles.copyright}>©2026. Todos os direitos reservados.</p>
    </footer>
  );
}
